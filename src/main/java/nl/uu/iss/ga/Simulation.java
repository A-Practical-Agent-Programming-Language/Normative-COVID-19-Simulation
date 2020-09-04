package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.reader.ActivityFileReader;
import main.java.nl.uu.iss.ga.model.reader.HouseholdReader;
import main.java.nl.uu.iss.ga.model.reader.LocationFileReader;
import main.java.nl.uu.iss.ga.model.reader.PersonReader;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DiseaseRiskContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import main.java.nl.uu.iss.ga.simulation.agent.environment.EnvironmentInterface;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.ExternalTriggerPlanScheme;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.util.ArgParse;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentArguments;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.defaults.messenger.DefaultMessenger;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.*;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;

public class Simulation {

    private static final int N_ITERATIONS = 100;

    public static void main(String[] args) {
        ArgParse parser = new ArgParse(args);
        new Simulation(parser);
    }

    private ArgParse arguments;
    private HouseholdReader householdReader;
    private PersonReader personReader;
    private LocationFileReader locationFileReader;
    private List<ActivityFileReader> activityFileReaders;

    private Platform platform;
    private TickExecutor tickExecutor;
    private EnvironmentInterface environmentInterface;
    private SimulationEngine simulationEngine;

    public Simulation(ArgParse arguments) {
        this.arguments = arguments;
        this.householdReader = new HouseholdReader(arguments.getHouseholdsFile());
        this.personReader = new PersonReader(arguments.getPersonsFile(), this.householdReader.getHouseholds());
        this.locationFileReader = new LocationFileReader(arguments.getLocationsfile());
        this.activityFileReaders = new ArrayList<>();
        for(File f : arguments.getActivityFiles()) {
            this.activityFileReaders.add(
                    new ActivityFileReader(f, this.personReader.getPersons(), this.locationFileReader.getLocations()));
        }

        preparePlatform();
        createAgents();

        this.simulationEngine.start();
    }

    private void preparePlatform() {
        DefaultMessenger messenger = new DefaultMessenger();
        this.tickExecutor = getLocalTickExecutor(); // TODO make it use Matrix is specified in args
        this.platform = Platform.newPlatform(tickExecutor, messenger);

        this.environmentInterface = new EnvironmentInterface(this.locationFileReader.getLocationsByIDMap());
        this.simulationEngine = new DefaultSimulationEngine(this.platform, N_ITERATIONS, this.environmentInterface);
    }

    private TickExecutor getLocalTickExecutor() {
        return new DefaultBlockingTickExecutor(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private TickExecutor getMatrixTickExecutor() {
        return new MatrixTickExecutor(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private void createAgents() {
        for(ActivityFileReader activityFileReader : this.activityFileReaders) {
            for (ActivitySchedule schedule : activityFileReader.getActivitySchedules()) {
                createAgentFromSchedule(schedule);
            }
        }
    }

    private void createAgentFromSchedule(ActivitySchedule schedule) {
        AgentArguments arguments = new AgentArguments()
                .addContext(this.personReader.getPersons().get(schedule.getPerson()))
                .addContext(new NormContext())
                .addContext(new DiseaseRiskContext())
                .addContext(new BeliefContext(this.environmentInterface))
                .addGoalPlanScheme(new GoalPlanScheme())
                .addExternalTriggerPlanScheme(new ExternalTriggerPlanScheme());

        try {
            URI uri = new URI(null, String.format("agent-%04d", schedule.getPerson()), platform.getHost(), platform.getPort(), null, null, null);
            AgentID aid = new AgentID(uri);
            Agent agent = new Agent(this.platform, arguments, aid);
            for(Activity activity : schedule.getSchedule().values()) {
                agent.adoptGoal(activity);
            }
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
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
