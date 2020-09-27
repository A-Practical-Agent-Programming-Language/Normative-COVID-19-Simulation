package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.reader.ActivityFileReader;
import main.java.nl.uu.iss.ga.model.reader.HouseholdReader;
import main.java.nl.uu.iss.ga.model.reader.LocationFileReader;
import main.java.nl.uu.iss.ga.model.reader.PersonReader;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DiseaseRiskContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.EnvironmentTriggerPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.ExternalTriggerPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.NormPlanScheme;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import main.java.nl.uu.iss.ga.util.ArgParse;
import main.java.nl.uu.iss.ga.util.Methods;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentArguments;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.defaults.messenger.DefaultMessenger;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.DefaultBlockingTickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.DefaultSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.SimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.matrix.MatrixTickExecutor;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Simulation {

    public static void main(String[] args) {
        ArgParse parser = new ArgParse(args);
        new Simulation(parser);
    }

    // TODO, check if all agents start and end home (sanity check when processing schedules)
    // TODO check if gaps aren't too weird

    private ArgParse arguments;
    private HouseholdReader householdReader;
    private PersonReader personReader;
    private LocationFileReader locationFileReader;
    private List<ActivityFileReader> activityFileReaders;

    private Platform platform;
    private TickExecutor<CandidateActivity> tickExecutor;
    private EnvironmentInterface environmentInterface;
    private SimulationEngine<CandidateActivity> simulationEngine;

    public Simulation(ArgParse arguments) {
        this.arguments = arguments;
        this.householdReader = new HouseholdReader(arguments.getHouseholdsFile(),1 - arguments.getFractionliberal());
        this.personReader = new PersonReader(arguments.getPersonsFile(), this.householdReader.getHouseholds());
        this.locationFileReader = new LocationFileReader(arguments.getLocationsfile());
        this.activityFileReaders = new ArrayList<>();
        for(File f : arguments.getActivityFiles()) {
            this.activityFileReaders.add(
                    new ActivityFileReader(f, this.personReader.getPersons(), this.locationFileReader.getLocations()));
        }

        preparePlatform();
        createAgents();

        try {
            this.simulationEngine.start();
        } catch (Exception e ) {
            e.printStackTrace();
        }
    }

    private void preparePlatform() {
        DefaultMessenger messenger = new DefaultMessenger();
        this.tickExecutor = getLocalTickExecutor(); // TODO make it use Matrix is specified in args
        this.platform = Platform.newPlatform(tickExecutor, messenger);

        this.environmentInterface = new EnvironmentInterface(platform, this.arguments.getRandom(), this.locationFileReader.getLocationsByIDMap());
        this.simulationEngine = new DefaultSimulationEngine<>(this.platform, arguments.getIterations(), this.environmentInterface);
    }

    private TickExecutor<CandidateActivity> getLocalTickExecutor() {
        return new DefaultBlockingTickExecutor<>(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private TickExecutor<CandidateActivity> getMatrixTickExecutor() {
        return new MatrixTickExecutor<>(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private void createAgents() {
        for(ActivityFileReader activityFileReader : this.activityFileReaders) {
            for (ActivitySchedule schedule : activityFileReader.getActivitySchedules()) {
//                if(schedule.getPerson() == 5633292) {
                    schedule.splitActivitiesByDay();
                    createAgentFromSchedule(schedule);
//                }
            }
        }
    }

    private void createAgentFromSchedule(ActivitySchedule schedule) {
        boolean isLiberal = this.householdReader.getHouseholds().get(schedule.getHousehold()).isLiberal();
        double initialGovernmentAttitude = Methods.nextSkewedBoundedDouble(
                arguments.getRandom(), isLiberal ? arguments.getModeliberal() : arguments.getModeconservative());

        LocationEntry homeLocation = findHomeLocation(schedule);
        AgentArguments<CandidateActivity> arguments = new AgentArguments<CandidateActivity>()
                .addContext(this.personReader.getPersons().get(schedule.getPerson()))
                .addContext(schedule)
                .addContext(new NormContext())
                .addContext(new DiseaseRiskContext())
                .addContext(new BeliefContext(this.environmentInterface, homeLocation,
                        initialGovernmentAttitude))
                .addContext(new DayPlanContext())
                .addExternalTriggerPlanScheme(new NormPlanScheme())
                .addExternalTriggerPlanScheme(new EnvironmentTriggerPlanScheme())
                .addExternalTriggerPlanScheme(new ExternalTriggerPlanScheme())
                .addGoalPlanScheme(new GoalPlanScheme());
        try {
            URI uri = new URI(null, String.format("agent-%04d", schedule.getPerson()), platform.getHost(), platform.getPort(), null, null, null);
            AgentID aid = new AgentID(uri);
            Agent<CandidateActivity> agent = new Agent<>(this.platform, arguments, aid);
            for(Activity activity : schedule.getSchedule().values()) {
                agent.adoptGoal(activity);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
    }

    private LocationEntry findHomeLocation(ActivitySchedule schedule) {
        for(Activity activity : schedule.getSchedule().values()) {
            if(activity.getActivityType().equals(ActivityType.HOME)) {
                return activity.getLocation();
            }
        }
        System.err.println("No home found! Checked " + schedule.getSchedule().size() + " values");
        return null;
    }

    /**
     * There are a number of detailedActivity codes for which I do not currently have the translation.
     * This is not necessarily a problem, since we will hardly be using these detailed activities.
     *
     * This method prints those codes, along with the ActivityType they were paired with.
     */
    private void printUnknownDetailedActivityNumbers() {
        for(ActivityFileReader reader : this.activityFileReaders) {
            if(reader.failedDetailedActivities.size() > 0) {
                System.out.println("Found the following detailed activity numbers that are not in the dictionary");
                for(int missedNumbers : reader.failedDetailedActivities.keySet()) {
                    System.out.printf("\t%d \t%s\n", missedNumbers, reader.failedDetailedActivities.get(missedNumbers).toString());
                }
            }
        }
    }

}
