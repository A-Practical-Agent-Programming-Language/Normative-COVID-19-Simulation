package main.java.nl.uu.iss.ga.util.config;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.reader.ActivityFileReader;
import main.java.nl.uu.iss.ga.model.reader.HouseholdReader;
import main.java.nl.uu.iss.ga.model.reader.LocationFileReader;
import main.java.nl.uu.iss.ga.model.reader.PersonReader;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.simulation.EnvironmentInterface;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.EnvironmentTriggerPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.NormPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.AdjustTrustAttitudeGoal;
import main.java.nl.uu.iss.ga.util.DirectObservationNotifierNotifier;
import main.java.nl.uu.iss.ga.util.Methods;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentArguments;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import org.tomlj.TomlArray;
import org.tomlj.TomlTable;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ConfigModel {

    private static final Logger LOGGER = Logger.getLogger(ConfigModel.class.getName());

    private final double fractionLiberal;
    private Random random;
    private final List<AgentID> agents = new ArrayList<>();
    private final List<File> activityFiles;
    private final List<File> householdFiles;
    private final List<File> personFiles;
    private final List<File> locationsFiles;
    private final File stateFile;

    private HouseholdReader householdReader;
    private PersonReader personReader;
    private LocationFileReader locationFileReader;
    private ActivityFileReader activityFileReader;
    private AgentStateMap agentStateMap;

    private final ArgParse arguments;
    private final TomlTable table;
    private final String name;
    private final int fipsCode;

    private String outputFileName;

    public ConfigModel(ArgParse arguments, String name, TomlTable table) throws Exception {
        this.arguments = arguments;
        this.name = name;
        this.table = table;
        this.fipsCode = table.getLong("fipscode").intValue();

        if(this.table.contains("fractionLiberal")) {
            this.fractionLiberal = this.table.getDouble("fractionLiberal");
        } else {
            throw new Exception(String.format("The fractionLiberal for the county %s is not specified", this.name));
        }

        this.activityFiles = getFiles("activities", true);
        this.householdFiles = getFiles("households", true);
        this.personFiles = getFiles("persons", true);
        this.locationsFiles = getFiles("locations", false);
        this.stateFile = getFile("statefile", false);

        if(this.table.contains("seed")) {
            this.random = new Random(table.getLong("seed"));
        } else {
            this.random = new Random();
        }

        createOutFileName();
    }

    public void loadFiles() {
        this.householdReader = new HouseholdReader(this.householdFiles, 1 - this.fractionLiberal, this.random);
        this.personReader = new PersonReader(this.personFiles, this.householdReader.getHouseholds());
        this.locationFileReader = new LocationFileReader(this.locationsFiles);
        this.activityFileReader = new ActivityFileReader(this.activityFiles, this.locationFileReader.getLocations());
        this.agentStateMap = this.stateFile == null ?
            new AgentStateMap(this.personReader.getPersons(), this.random) :
            new AgentStateMap(Collections.singletonList(this.stateFile), this.random);
    }

    public Callable<Void> getAsyncLoadFiles() {
        return () -> {
            loadFiles();
            return null;
        };
    }

    public void createAgents(Platform platform, ObservationNotifier observationNotifier, EnvironmentInterface environmentInterface) {
        for (ActivitySchedule schedule : this.activityFileReader.getActivitySchedules()) {
            schedule.splitActivitiesByDay();
            createAgentFromSchedule(platform, observationNotifier, environmentInterface, schedule);
        }
    }

    private void createAgentFromSchedule(Platform platform, ObservationNotifier observationNotifier, EnvironmentInterface environmentInterface, ActivitySchedule schedule) {
        boolean isLiberal = this.householdReader.getHouseholds().get(schedule.getHousehold()).isLiberal();
        double initialGovernmentAttitude = isLiberal ? this.arguments.getLiberalTrustDistribution().sample() : this.arguments.getConservativeTrustDistribution().sample();

        LocationEntry homeLocation = this.findHomeLocation(schedule);

        NormContext normContext = new NormContext();
        LocationHistoryContext locationHistoryContext = new LocationHistoryContext();
        BeliefContext beliefContext = new BeliefContext(environmentInterface, homeLocation, initialGovernmentAttitude);
        AgentArguments<CandidateActivity> arguments = new AgentArguments<CandidateActivity>()
                .addContext(this.personReader.getPersons().get(schedule.getPerson()))
                .addContext(schedule)
                .addContext(normContext)
                .addContext(locationHistoryContext)
                .addContext(beliefContext)
                .addContext(new DayPlanContext())
                .addExternalTriggerPlanScheme(new NormPlanScheme())
                .addExternalTriggerPlanScheme(new EnvironmentTriggerPlanScheme())
                .addGoalPlanScheme(new GoalPlanScheme());
        try {
            URI uri = new URI(null, String.format("agent-%04d", schedule.getPerson()),
                    platform.getHost(), platform.getPort(), null, null, null);
            AgentID aid = new AgentID(uri);
            Agent<CandidateActivity> agent = new Agent<>(platform, arguments, aid);
            for(Activity activity : schedule.getSchedule().values()) {
                agent.adoptGoal(activity);
            }
            agent.adoptGoal(new AdjustTrustAttitudeGoal(this.arguments.getFatigue(), this.arguments.getFatigueStart()));
            this.agents.add(aid);
            this.agentStateMap.addAgent(aid, schedule.getPerson(), this.fipsCode);
            beliefContext.setAgentID(aid);
            ((DirectObservationNotifierNotifier) observationNotifier).addNormContext(aid, schedule.getPerson(), normContext);
            ((DirectObservationNotifierNotifier) observationNotifier).addLocationHistoryContext(aid, schedule.getPerson(), locationHistoryContext);
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE, "Failed to create AgentID for agent " + schedule.getPerson(), e);
        }
    }

    private LocationEntry findHomeLocation(ActivitySchedule schedule) {
        long lid = this.personReader.getPersons().get(schedule.getPerson()).getHousehold().getLocationID();

        for(Activity activity : schedule.getSchedule().values()) {
            if(activity.getActivityType().equals(ActivityType.HOME) && activity.getLocation().getLocationID() == lid) {
                return activity.getLocation();
            }
        }
        LOGGER.log(Level.SEVERE,
                String.format("No home location entry found for lid %d for person %d. Checked %d values",
                        lid, schedule.getPerson(), schedule.getSchedule().size()));
        return null;
    }

    private List<File> getFiles(String key, boolean required) throws Exception {
        List<File> files = new ArrayList<>();
        if(this.table.contains(key)) {
            TomlArray arr = this.table.getArray(key);
            for(int i = 0; i < arr.size(); i++) {
                files.add(ArgParse.findFile(new File(arr.getString(i))));
            }
        } else if (required) {
            throw new Exception(String.format("Missing required key %s for county %s", key, this.name));
        }
        return files;
    }

    private File getFile(String key, boolean required) throws Exception {
        File f = null;
        if(this.table.contains(key)) {
            f = ArgParse.findFile(new File(this.table.getString(key)));
        } else if (required) {
            throw new Exception(String.format("Missing required key %s for county %s", key, this.name));
        }
        return f;
    }

    private void createOutFileName() {
        String descriptor = this.arguments.getDescriptor() == null ? "" : this.arguments.getDescriptor();
        if(this.arguments.getDescriptor() != null) {
            if(!(descriptor.startsWith("-") || descriptor.startsWith("_")))
                descriptor = "-" + descriptor;
            if (!(descriptor.endsWith("-") | descriptor.endsWith("_")))
                descriptor += "-";
        }

        this.outputFileName = String.format(
                "radius-of-gyration-%s-%s-%s%s.csv",
                this.name,
                this.fipsCode,
                this.arguments.getNode() >= 0 ? "node" + this.arguments.getNode() : "",
                descriptor
        );
    }

    public double getFractionLiberal() {
        return fractionLiberal;
    }

    public Random getRandom() {
        return random;
    }

    public List<File> getActivityFiles() {
        return activityFiles;
    }

    public List<File> getHouseholdFiles() {
        return householdFiles;
    }

    public List<File> getPersonFiles() {
        return personFiles;
    }

    public List<File> getLocationsFiles() {
        return locationsFiles;
    }

    public List<AgentID> getAgents() {
        return agents;
    }

    public File getStateFile() {
        return stateFile;
    }

    public HouseholdReader getHouseholdReader() {
        return householdReader;
    }

    public PersonReader getPersonReader() {
        return personReader;
    }

    public LocationFileReader getLocationFileReader() {
        return locationFileReader;
    }

    public ActivityFileReader getActivityFileReader() {
        return activityFileReader;
    }

    public AgentStateMap getAgentStateMap() {
        return agentStateMap;
    }

    public String getName() {
        return name;
    }

    public int getFipsCode() {
        return fipsCode;
    }

    public void setAgentStateMap(AgentStateMap map) {
        this.agentStateMap = map;
    }

    public String getOutFileName() {
        return this.outputFileName;
    }
}
