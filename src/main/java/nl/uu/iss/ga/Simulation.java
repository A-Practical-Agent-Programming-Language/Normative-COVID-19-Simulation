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
import main.java.nl.uu.iss.ga.pansim.PansimSimulationEngine;
import main.java.nl.uu.iss.ga.simulation.agent.context.*;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.EnvironmentTriggerPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.NormPlanScheme;
import main.java.nl.uu.iss.ga.simulation.environment.AgentStateMap;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import main.java.nl.uu.iss.ga.util.ArgParse;
import main.java.nl.uu.iss.ga.util.DirectObservationNotifierNotifier;
import main.java.nl.uu.iss.ga.util.Methods;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentArguments;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.defaults.messenger.DefaultMessenger;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.*;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.logging.Level;

public class Simulation {

    public static void main(String[] args) {
        ArgParse parser = new ArgParse(args);
        new Simulation(parser);
    }

    // TODO, check if all agents start and end home (sanity check when processing schedules)
    // TODO check if gaps aren't too weird

    private final ArgParse arguments;
    private final HouseholdReader householdReader;
    private final PersonReader personReader;
    private final LocationFileReader locationFileReader;
    private final ActivityFileReader activityFileReader;

    private Platform platform;
    private TickExecutor<CandidateActivity> tickExecutor;
    private EnvironmentInterface environmentInterface;
    private SimulationEngine<CandidateActivity> simulationEngine;

    private final AgentStateMap agentStateMap;
    private ObservationNotifier observationNotifier;

    public Simulation(ArgParse arguments) {
        this.arguments = arguments;

        this.householdReader = new HouseholdReader(arguments.getHouseholdsFiles(), 1 - arguments.getFractionliberal());
        this.personReader = new PersonReader(arguments.getPersonsFiles(), householdReader.getHouseholds());
        this.locationFileReader = new LocationFileReader(arguments.getLocationsfiles());
        this.activityFileReader = new ActivityFileReader(arguments.getActivityFiles(), this.locationFileReader.getLocations());

        this.agentStateMap = arguments.getStatefiles() == null || arguments.getStatefiles().isEmpty() ?
                new AgentStateMap(this.personReader.getPersons(), arguments.getRandom()) :
                new AgentStateMap(arguments.getStatefiles(), arguments.getRandom());

        preparePlatform();
        createAgents();

        this.simulationEngine.start();
    }

    private void preparePlatform() {
        DefaultMessenger messenger = new DefaultMessenger();
        this.tickExecutor = new DefaultBlockingTickExecutor<>(this.arguments.getThreads(), this.arguments.getRandom());
        this.platform = Platform.newPlatform(tickExecutor, messenger);
        this.observationNotifier = new DirectObservationNotifierNotifier(this.platform);
        this.environmentInterface = new EnvironmentInterface(platform, this.observationNotifier, this.agentStateMap, this.arguments);
        this.simulationEngine = arguments.isConnectpansim() ? getPansimSimulationEngine() : getLocalSimulationEngine();
    }

    private SimulationEngine<CandidateActivity> getLocalSimulationEngine() {
        return new DefaultSimulationEngine<>(this.platform, arguments.getIterations(), this.environmentInterface);
    }

    private SimulationEngine<CandidateActivity> getPansimSimulationEngine() {
        return new PansimSimulationEngine(this.platform, this.observationNotifier, this.agentStateMap, this.environmentInterface);
    }

    private void createAgents() {
        for (ActivitySchedule schedule : this.activityFileReader.getActivitySchedules()) {
            schedule.splitActivitiesByDay();
            createAgentFromSchedule(schedule);
        }
    }

    private void createAgentFromSchedule(ActivitySchedule schedule) {
        boolean isLiberal = this.householdReader.getHouseholds().get(schedule.getHousehold()).isLiberal();
        double initialGovernmentAttitude = Methods.nextSkewedBoundedDouble(
                arguments.getRandom(), isLiberal ? arguments.getModeliberal() : arguments.getModeconservative());

        LocationEntry homeLocation = this.findHomeLocation(schedule);

        NormContext normContext = new NormContext();
        LocationHistoryContext locationHistoryContext = new LocationHistoryContext();
        BeliefContext beliefContext = new BeliefContext(this.environmentInterface, homeLocation,
                initialGovernmentAttitude);
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
            Agent<CandidateActivity> agent = new Agent<>(this.platform, arguments, aid);
            for(Activity activity : schedule.getSchedule().values()) {
                agent.adoptGoal(activity);
            }
            this.agentStateMap.addAgent(aid, schedule.getPerson());
            beliefContext.setAgentID(aid);
            ((DirectObservationNotifierNotifier) this.observationNotifier).addNormContext(aid, schedule.getPerson(), normContext);
            ((DirectObservationNotifierNotifier) this.observationNotifier).addLocationHistoryContext(aid, schedule.getPerson(), locationHistoryContext);
        } catch (URISyntaxException e) {
            Platform.getLogger().log(getClass(), Level.SEVERE, e);
        }
    }

    private LocationEntry findHomeLocation(ActivitySchedule schedule) {
        long lid = this.personReader.getPersons().get(schedule.getPerson()).getHousehold().getLocationID();

        for(Activity activity : schedule.getSchedule().values()) {
            if(activity.getActivityType().equals(ActivityType.HOME) && activity.getLocation().getLocationID() == lid) {
                return activity.getLocation();
            }
        }
        Platform.getLogger().log(getClass(), Level.SEVERE,
                String.format("No home location entry found for lid %d for person %d. Checked %d values",
                lid, schedule.getPerson(), schedule.getSchedule().size()));
        return null;
    }

    /**
     * There are a number of detailedActivity codes for which I do not currently have the translation.
     * This is not necessarily a problem, since we will hardly be using these detailed activities.
     *
     * This method prints those codes, along with the ActivityType they were paired with.
     */
    private void printUnknownDetailedActivityNumbers() {
        if(this.activityFileReader.failedDetailedActivities.size() > 0) {
            Platform.getLogger().log(getClass(), Level.WARNING,
                    "Found the following detailed activity numbers that are not in the dictionary");
            for(int missedNumbers : this.activityFileReader.failedDetailedActivities.keySet()) {
                Platform.getLogger().log(getClass(), Level.WARNING,
                String.format(
                        "\t%d \t%s",
                        missedNumbers,
                        this.activityFileReader.failedDetailedActivities.get(missedNumbers).toString()));
            }
        }
    }

}
