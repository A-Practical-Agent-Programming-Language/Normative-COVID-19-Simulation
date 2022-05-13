package nl.uu.iss.ga;

import nl.uu.cs.iss.ga.sim2apl.core.defaults.messenger.DefaultMessenger;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.step.SimulationEngine;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.norm.NormFactory;
import nl.uu.iss.ga.model.reader.NormScheduleReader;
import nl.uu.iss.ga.pansim.PansimSimulationEngine;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.simulation.DefaultTimingSimulationEngine;
import nl.uu.iss.ga.simulation.NoRescheduleBlockingStepExecutor;
import nl.uu.iss.ga.simulation.PansimEnvironmentInterface;
import nl.uu.iss.ga.util.CountNormApplication;
import nl.uu.iss.ga.util.DirectObservationNotifierNotifier;
import nl.uu.iss.ga.util.Java2APLLogger;
import nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.iss.ga.util.config.SimulationArguments;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Simulation {
    private static final Logger LOGGER = Logger.getLogger(Simulation.class.getName());
    public static final LocalDateTime instantiated = LocalDateTime.now();

    public static void main(String[] args) {
        SimulationArguments.parseArguments(args);
        new Simulation();
    }

    private final SimulationArguments arguments;
    private final NormScheduleReader normScheduleReader;

    private Platform platform;
    private final NoRescheduleBlockingStepExecutor<CandidateActivity> stepExecutor;
    private PansimEnvironmentInterface pansimEnvironmentInterface;
    private SimulationEngine<CandidateActivity> simulationEngine;

    private AgentStateMap agentStateMap;

    private ObservationNotifier observationNotifier;

    public Simulation() {
        this.arguments = SimulationArguments.getInstance();
        this.normScheduleReader = new NormScheduleReader(arguments.getNormFile());
        this.stepExecutor = new NoRescheduleBlockingStepExecutor<>(this.arguments.getThreads(), this.arguments.getSystemWideRandom());

        if(arguments.isCountAffectedAgents()) {
            arguments.setSuppressCalculations(true);
        }

        readCountyData();
        preparePlatform();

        for(ConfigModel county : this.arguments.getCounties()) {
            county.createAgents(this.platform, this.observationNotifier, this.pansimEnvironmentInterface);
        }

        if (arguments.isCountAffectedAgents()) {
            CountNormApplication normCounter = new CountNormApplication(
                    this.platform,
                    this.arguments,
                    this.pansimEnvironmentInterface,
                    this.agentStateMap,
                    NormFactory.instantiateAllNorms()
            );
            normCounter.writeAffectedAgentsToFile();
            System.exit(0);
        } else {
            this.pansimEnvironmentInterface.setSimulationStarted();
            this.simulationEngine.start();
        }
    }

    private void readCountyData() {
        if(arguments.getCounties().size() > 1) {
            List<Callable<Void>> callables = new ArrayList<>();
            arguments.getCounties().forEach(x -> callables.add(x.getAsyncLoadFiles()));
            try {
                this.stepExecutor.useExecutorForTasks(callables);
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Failed to read files", e);
                System.exit(9);
            }
        } else if (!arguments.getCounties().isEmpty()){
            arguments.getCounties().get(0).loadFiles();
        } else {
            LOGGER.log(Level.INFO, "No counties found");
            System.exit(0);
        }
        this.agentStateMap = mergeStateMaps();
    }

    private void preparePlatform() {
        DefaultMessenger<CandidateActivity> messenger = new DefaultMessenger<>();

        this.platform = Platform.newPlatform(stepExecutor, messenger);
        this.platform.setLogger(new Java2APLLogger());
        this.observationNotifier = new DirectObservationNotifierNotifier(this.platform);
        this.pansimEnvironmentInterface = new PansimEnvironmentInterface(
                platform,
                this.observationNotifier,
                this.agentStateMap,
                this.normScheduleReader,
                this.arguments
        );
        this.simulationEngine = arguments.isConnectpansim() ? getPansimSimulationEngine() : getLocalSimulationEngine();
    }

    private SimulationEngine<CandidateActivity> getLocalSimulationEngine() {
//        return new DefaultSimulationEngine<>(this.platform, (int)arguments.getIterations(), this.environmentInterface);
        return new DefaultTimingSimulationEngine<>(this.platform, this.arguments, (int)this.arguments.getIterations(), this.pansimEnvironmentInterface);
    }

    private SimulationEngine<CandidateActivity> getPansimSimulationEngine() {
        return new PansimSimulationEngine(this.platform, this.arguments, this.observationNotifier, this.agentStateMap, this.pansimEnvironmentInterface);
    }

    private AgentStateMap mergeStateMaps() {
        AgentStateMap merged = AgentStateMap.merge(
                arguments.getOutputDir(),
                arguments.isSuppressCalculations(),
                this.arguments.getCounties().stream().map(ConfigModel::getAgentStateMap).collect(Collectors.toList())
        );
        this.arguments.getCounties().forEach(x -> x.setAgentStateMap(merged));
        return merged;
    }
}
