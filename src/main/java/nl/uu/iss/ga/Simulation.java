package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.reader.ActivityFileReader;
import main.java.nl.uu.iss.ga.model.reader.HouseholdReader;
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

import java.net.URI;
import java.net.URISyntaxException;

public class Simulation {

    public static void main(String[] args) {
        ArgParse parser = new ArgParse(args);
        new Simulation(parser);
    }

    private ArgParse arguments;
    private HouseholdReader householdReader;
    private PersonReader personReader;
    private ActivityFileReader activityFileReader;

    private Platform platform;
    private TickExecutor tickExecutor;
    private EnvironmentInterface environmentInterface;
    private SimulationEngine simulationEngine;

    public Simulation(ArgParse arguments) {
        this.arguments = arguments;
        this.householdReader = new HouseholdReader(arguments.getHouseholdsFile());
        this.personReader = new PersonReader(arguments.getPersonsFile(), this.householdReader.getHouseholds());
        this.activityFileReader = new ActivityFileReader(arguments.getActivityFile(), this.personReader.getPersons());

        preparePlatform();
        createAgents();

        this.simulationEngine.start();
    }

    private void preparePlatform() {
        DefaultMessenger messenger = new DefaultMessenger();
        this.tickExecutor = getLocalTickExecutor(); // TODO make it use Matrix is specified in args
        this.platform = Platform.newPlatform(tickExecutor, messenger);

        this.environmentInterface = new EnvironmentInterface();
        this.simulationEngine = new DefaultSimulationEngine(this.platform, 100, this.environmentInterface);
    }

    private TickExecutor getLocalTickExecutor() {
        return new DefaultBlockingTickExecutor(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private TickExecutor getMatrixTickExecutor() {
        return new MatrixTickExecutor(this.arguments.getThreads(), this.arguments.getRandom());
    }

    private void createAgents() {
        for(ActivitySchedule schedule : this.activityFileReader.getActivitySchedules()) {
            createAgentFromSchedule(schedule);
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

}
