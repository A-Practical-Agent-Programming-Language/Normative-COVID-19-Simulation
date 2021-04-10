package main.java.nl.uu.iss.ga.pansim;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import main.java.nl.uu.iss.ga.simulation.NoRescheduleBlockingTickExecutor;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.tracking.ScheduleTrackerGroup;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.AbstractSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;
import org.apache.arrow.memory.RootAllocator;
import py4j.GatewayServer;
import py4j.GatewayServerListener;
import py4j.Py4JServerConnection;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class PansimSimulationEngine extends AbstractSimulationEngine<CandidateActivity> {

    private static final Logger LOGGER = Logger.getLogger(PansimSimulationEngine.class.getName());

    private final GatewayServer gatewayServer;

    private final AgentStateMap agentStateMap;
    private final ObservationNotifier observationNotifier;
    private final ArgParse arguments;

    private final TickExecutor<CandidateActivity> executor;

    private byte[] next_state_df_raw;
    private byte[] next_visit_df_raw;

    private long tickEnd = -1;
    private final ScheduleTrackerGroup timingsTracker;

    public RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    public PansimSimulationEngine(Platform platform, ArgParse arguments, ObservationNotifier observationNotifier, AgentStateMap agentStateMap, TickHookProcessor<CandidateActivity>... processors) {
        super(platform, processors);
        this.gatewayServer = new GatewayServer(this);
        this.agentStateMap = agentStateMap;
        this.arguments = arguments;
        this.observationNotifier = observationNotifier;
        this.executor = platform.getTickExecutor();
        if(arguments.saveStateDataFrames()) {
            prepare_output();
        }

        Path outputDir = Path.of(arguments.getOutputDir()).isAbsolute() ?
                Path.of(arguments.getOutputDir()) : Path.of("output", arguments.getOutputDir());

        this.timingsTracker = new ScheduleTrackerGroup(
                outputDir.toFile().getAbsolutePath(),
                "timings.csv",
                List.of("tick", "communication", "stateExtracted", "visitsExtracted",
                        "visitsProcessed","prehook", "deliberation", "visitsEncoded", "stateEncoded", "posthook")
                );
    }

    private void runFirstTick() {
        try {
            byte[] first_state_df_raw = StateDataFrame.fromAgentStateMapMultiThread(
                    ((NoRescheduleBlockingTickExecutor<CandidateActivity>) this.executor),
                    this.arguments.getThreads(),
                    this.agentStateMap.getAllAgentStates(),
                    this.allocator
            ).toBytes();
            byte[] null_visit_output_raw = new VisitResultDataFrame(this.allocator).toBytes();
            this.runBehaviorModel(first_state_df_raw, null_visit_output_raw);
            LOGGER.log(Level.INFO, "Pansim Behavior Server Started");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to run first iteration of the behavior model", e);
            System.exit(3);
        }
    }

    public void runBehaviorModel(byte[] cur_state_df_raw, byte[] visit_output_df_raw) throws IOException {
        long millis = System.currentTimeMillis();

        HashMap<String, String> timingsMap = new HashMap<>();
        timingsMap.put("tick", Integer.toString(this.executor.getCurrentTick()));
        timingsMap.put("pansim", this.tickEnd > -1 ? Long.toString(millis - tickEnd) : "");

        // TODO Cannot be parallelized
        StateDataFrame state_output_df = new StateDataFrame(cur_state_df_raw, allocator);
        timingsMap.put("stateExtracted", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        // TODO Multithreaded done
        this.agentStateMap.fromDataFrameMultiThreaded(state_output_df, arguments.getThreads(), (NoRescheduleBlockingTickExecutor<CandidateActivity>) executor);
//        this.agentStateMap.fromDataFrameSingleThead(state_output_df);
        timingsMap.put("stateProcessed", Long.toString(System.currentTimeMillis() - millis));
        state_output_df.close();
        millis = System.currentTimeMillis();

        // TODO cannot be parallelized
        VisitResultDataFrame visit_output_df = new VisitResultDataFrame(visit_output_df_raw, allocator);
        timingsMap.put("visitsExtracted", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        // TODO Multithreaded done
//        process_visit_output_single_thread(visit_output_df);
        process_visit_output(visit_output_df);
        timingsMap.put("visitsProcessed", Long.toString(System.currentTimeMillis() - millis));
        visit_output_df.close();
        millis = System.currentTimeMillis();

        LOGGER.log(Level.FINE, String.format(
                "Received new state dataframe with %d rows", this.agentStateMap.getNumberOfStates()));
        LOGGER.log(Level.FINE, String.format(
                "Received new visit output dataframe with %d rows", visit_output_df.getSchemaRoot().getRowCount()));

        // TODO can we improve notifying of norms?
        processTickPreHooks(this.executor.getCurrentTick());
        timingsMap.put("prehook", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        // Multithreaded done
//        HashMap<AgentID, List<CandidateActivity>> agentActions = this.executor.doTick();
        List<Future<DeliberationResult<CandidateActivity>>> agentActions = ((NoRescheduleBlockingTickExecutor<CandidateActivity>) this.executor).doTick(timingsMap);
        timingsMap.put("deliberation", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        // Prepare results for pansim and misuse loop to set disease state on activities
        // TODO parallelize
        VisitDataFrame visitDf = VisitDataFrame.fromAgentActionsMultiThread(agentActions, this.agentStateMap, allocator, arguments.getThreads(), ((NoRescheduleBlockingTickExecutor<CandidateActivity>) this.executor));
//        VisitDataFrame visitDf = VisitDataFrame.fromAgentActionsSingleThread(agentActions, this.agentStateMap, this.allocator);
        this.next_visit_df_raw = visitDf.toBytes();
        visitDf.close();
        timingsMap.put("visitsEncoded", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        // TODO Multithreaded done
        StateDataFrame stateDf = StateDataFrame.fromAgentStateMapMultiThread(((NoRescheduleBlockingTickExecutor<CandidateActivity>) this.executor), this.arguments.getThreads(), this.agentStateMap.getAllAgentStates(), this.allocator);
//        StateDataFrame stateDf = StateDataFrame.fromAgentStateMapSingleThead(this.agentStateMap.getAllAgentStates(), this.allocator).toBytes();
        this.next_state_df_raw = stateDf.toBytes();
        stateDf.close();
        timingsMap.put("stateEncoded", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        if(arguments.saveStateDataFrames()) {
            write_state_dataframes(cur_state_df_raw, next_state_df_raw);
        }

        // TODO don't parallelize, as we suppress calculations anyway when doing scaling?
        processTickPostHook(this.executor.getCurrentTick() - 1, this.executor.getLastTickDuration(), new HashMap<>());
        timingsMap.put("posthook", Long.toString(System.currentTimeMillis() - millis));

        this.timingsTracker.writeKeyMapToFile(
                arguments.getStartdate().plusDays(this.executor.getCurrentTick()),
                timingsMap
        );

        this.tickEnd = System.currentTimeMillis();
    }

    public byte[] getNextStateDataFrame() {
        LOGGER.log(Level.FINE, String.format(
                "Returning state dataframe for tick %d", this.executor.getCurrentTick()));
        return next_state_df_raw;
    }

    public byte[] getNextVisitDataFrame() {
        LOGGER.log(Level.FINE, String.format(
                "Returning next visit dataframe for tick %d", this.executor.getCurrentTick()));
        return next_visit_df_raw;
    }

    public void shutdown() {
        if(this.gatewayServer != null) {
            gatewayServer.shutdown();
            LOGGER.log(Level.INFO, "Pansim Behavior Server Shutdown");
        }
        executor.shutdown();
        System.exit(0);
    }

    public boolean cleanup() throws Exception{
        LOGGER.log(Level.INFO, "Pansim sent cleanup signal.");
        processSimulationFinishedHook(this.executor.getCurrentTick(), this.executor.getLastTickDuration());
        executor.shutdown();
        return true;
    }

    private void process_visit_output_single_thread(VisitResultDataFrame visit_output_df) {
        for (int i = 0; i < visit_output_df.getSchemaRoot().getRowCount(); i++) {
            LocationHistoryContext.Visit visit = visit_output_df.getAgentVisit(i);
            this.observationNotifier.notifyVisit(visit.getPersonID(), this.executor.getCurrentTick(), visit);
        }
    }

    private void process_visit_output(VisitResultDataFrame visit_output_df) {
        NoRescheduleBlockingTickExecutor<CandidateActivity> exec = (NoRescheduleBlockingTickExecutor<CandidateActivity>) this.executor;
        List<Callable<Void>> runnables = new ArrayList<>();
        for(int i = 0; i < this.arguments.getThreads(); i++) {
            runnables.add(new ProcessVisitOutput(observationNotifier, visit_output_df, i, arguments.getThreads(), executor.getCurrentTick()));
        }

        try {
            exec.useExecutorForTasks(runnables);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Failed to process tick visits concurrently. Exiting");
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(10);
        }
    }

    static class ProcessVisitOutput implements Callable<Void> {

        ObservationNotifier notifier;
        VisitResultDataFrame dataFrame;
        private final int thread;
        private final int threads_total;
        int currentTick;

        public ProcessVisitOutput(ObservationNotifier notifier, VisitResultDataFrame dataFrame, int i, int threads, int currentTick) {
            this.notifier = notifier;
            this.dataFrame = dataFrame;
            this.currentTick = currentTick;
            this.thread = i;
            this.threads_total = threads;
        }

        @Override
        public Void call() {
            int start = thread * dataFrame.getSchemaRoot().getRowCount() / threads_total;
            int end = (thread + 1) * dataFrame.getSchemaRoot().getRowCount() / threads_total;
            if (end > dataFrame.getSchemaRoot().getRowCount()) end = dataFrame.getSchemaRoot().getRowCount();

            for(int i = start; i < end; i++) {
                LocationHistoryContext.Visit visit = this.dataFrame.getAgentVisit(i);
                this.notifier.notifyVisit(visit.getPersonID(), currentTick, visit);
            }
            return null;
        }
    }

    @Override
    public boolean start() {
        runFirstTick();
        gatewayServer.addListener(new GatewayServerListener() {
            @Override
            public void connectionError(Exception e) { }

            @Override
            public void connectionStarted(Py4JServerConnection gatewayConnection) { }

            @Override
            public void connectionStopped(Py4JServerConnection gatewayConnection) {
                shutdown();
            }

            @Override
            public void serverError(Exception e) {  }

            @Override
            public void serverPostShutdown() {  }

            @Override
            public void serverPreShutdown() {  }

            @Override
            public void serverStarted() {   }

            @Override
            public void serverStopped() {
                shutdown();
            }
        });
        gatewayServer.start();
        return true;
    }

    File parentDir;

    private void prepare_output() {
        String parent =
                (Path.of(
                        arguments.getOutputDir()).isAbsolute() ?
                        Path.of(arguments.getOutputDir()) :
                        Path.of("output", arguments.getOutputDir())
                ).toFile().getAbsolutePath();

        this.parentDir = Paths.get(parent, "state_df_raw").toFile();
        if (!(this.parentDir.exists() || this.parentDir.mkdirs())) {
            LOGGER.log(Level.SEVERE, "Failed to create state dataframe output directory " + this.parentDir.getAbsolutePath());
        }
    }

    private void write_state_dataframe_to_file(byte[] state_dataframe, File out) {
        try {
            if (!(out.exists() || out.createNewFile())) {
                throw new IOException("Failed to create file " + out.getAbsolutePath());
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create state data frame file " + out.getAbsolutePath());
            LOGGER.log(Level.SEVERE, e.getMessage());
            return;
        }

        try(FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(state_dataframe);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write state data frame to file " + out.getAbsolutePath());
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private void write_state_dataframes(byte[] current_state_df_raw, byte[] next_state_df_raw) {
        File fout_pansim = Paths.get(this.parentDir.getAbsolutePath(), String.format("state_df_raw_tick_%03d_pansim.raw", this.executor.getCurrentTick())).toFile();
        File fout_behavior = Paths.get(this.parentDir.getAbsolutePath(), String.format("state_df_raw_tick_%03d_behavior.raw", this.executor.getCurrentTick())).toFile();

        write_state_dataframe_to_file(current_state_df_raw, fout_pansim);
        write_state_dataframe_to_file(next_state_df_raw, fout_behavior);
    }
}
