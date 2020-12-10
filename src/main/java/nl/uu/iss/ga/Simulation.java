package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.reader.NormScheduleReader;
import main.java.nl.uu.iss.ga.pansim.PansimSimulationEngine;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.simulation.EnvironmentInterface;
import main.java.nl.uu.iss.ga.util.DirectObservationNotifierNotifier;
import main.java.nl.uu.iss.ga.util.Java2APLLogger;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.cs.iss.ga.sim2apl.core.defaults.messenger.DefaultMessenger;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.DefaultBlockingTickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.DefaultSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.SimulationEngine;

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
        ArgParse parser = new ArgParse(args);
        new Simulation(parser);
    }

    private final ArgParse arguments;
    private final NormScheduleReader normScheduleReader;

    private Platform platform;
    private DefaultBlockingTickExecutor<CandidateActivity> tickExecutor;
    private EnvironmentInterface environmentInterface;
    private SimulationEngine<CandidateActivity> simulationEngine;

    private AgentStateMap agentStateMap;

    private ObservationNotifier observationNotifier;

    public Simulation(ArgParse arguments) {
        this.arguments = arguments;
        this.normScheduleReader = new NormScheduleReader(arguments.getNormFile());
        this.tickExecutor = new DefaultBlockingTickExecutor<>(this.arguments.getThreads(), this.arguments.getSystemWideRandom());

        readCountyData();
        preparePlatform();

        for(ConfigModel county : this.arguments.getCounties()) {
            county.createAgents(this.platform, this.observationNotifier, this.environmentInterface);
        }

        this.environmentInterface.setSimulationStarted();
        this.simulationEngine.start();
    }

    private void readCountyData() {
        if(arguments.getCounties().size() > 1) {
            List<Callable<Void>> callables = new ArrayList<>();
            arguments.getCounties().forEach(x -> callables.add(x.getAsyncLoadFiles()));
            try {
                this.tickExecutor.useExecutorForTasks(callables);
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
        DefaultMessenger messenger = new DefaultMessenger();

        this.platform = Platform.newPlatform(tickExecutor, messenger);
        this.platform.setLogger(new Java2APLLogger());
        this.observationNotifier = new DirectObservationNotifierNotifier(this.platform);
        this.environmentInterface = new EnvironmentInterface(
                platform,
                this.observationNotifier,
                this.agentStateMap,
                this.normScheduleReader,
                this.arguments
        );
        this.simulationEngine = arguments.isConnectpansim() ? getPansimSimulationEngine() : getLocalSimulationEngine();
    }

    private SimulationEngine<CandidateActivity> getLocalSimulationEngine() {
        return new DefaultSimulationEngine<>(this.platform, (int)arguments.getIterations(), this.environmentInterface);
    }

    private SimulationEngine<CandidateActivity> getPansimSimulationEngine() {
        return new PansimSimulationEngine(this.platform, this.observationNotifier, this.agentStateMap, this.environmentInterface);
    }

    private AgentStateMap mergeStateMaps() {
        AgentStateMap merged = AgentStateMap.merge(this.arguments.getCounties().stream().map(ConfigModel::getAgentStateMap).collect(Collectors.toList()));
        this.arguments.getCounties().forEach(x -> x.setAgentStateMap(merged));
        return merged;
    }
}
