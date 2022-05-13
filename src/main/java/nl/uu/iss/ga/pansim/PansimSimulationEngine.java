package nl.uu.iss.ga.pansim;

import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.step.AbstractSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.step.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.step.StepExecutor;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.pansim.state.StateDataFrame;
import nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import nl.uu.iss.ga.simulation.NoRescheduleBlockingStepExecutor;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.util.Methods;
import nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.iss.ga.util.config.ArgParse;
import nl.uu.iss.ga.util.tracking.ScheduleTrackerGroup;
import org.apache.arrow.memory.RootAllocator;
import py4j.GatewayServer;
import py4j.GatewayServerListener;
import py4j.Py4JServerConnection;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
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

    private final StepExecutor<CandidateActivity> executor;

    private byte[] next_state_df_raw;
    private byte[] next_visit_df_raw;

    private long stepEnd = -1;
    private final ScheduleTrackerGroup timingsTracker;

    private String parentDir;
    private final String STATE_DATAFRAME_DIR_NAME = "state_df_raw";
    private final String VISITS_DATAFRAME_DIR_NAME = "visits_df_raw";

    public RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    @SafeVarargs
    public PansimSimulationEngine(
            Platform platform,
            ArgParse arguments,
            ObservationNotifier observationNotifier,
            AgentStateMap agentStateMap,
            EnvironmentInterface<CandidateActivity>... environmentInterfaces
    ) {
        super(platform, environmentInterfaces);
        this.gatewayServer = new GatewayServer(this);
        this.agentStateMap = agentStateMap;
        this.arguments = arguments;
        this.observationNotifier = observationNotifier;
        this.executor = platform.getStepExecutor();
        if(arguments.saveStateDataFrames()) {
            this.parentDir = prepare_output(STATE_DATAFRAME_DIR_NAME);
        }
        if(arguments.saveVisitsDataFrames()) {
            this.parentDir = prepare_output(VISITS_DATAFRAME_DIR_NAME);
        }

        this.timingsTracker = new ScheduleTrackerGroup(
                arguments.getOutputDir(),
                "timings.csv",
                List.of("timestep", "pansim", "stateExtracted", "visitsExtracted",
                        "visitsProcessed","stepStarting",
                        "reassignPointer", // TODO remove if not using DefaultTimingSimulaitonEngine
                        "deliberation",
                        "visitsEncoded", "stateEncoded", "stepFinished")
                );
    }

    private void runFirstTimeStep() {
        try {
            byte[] first_state_df_raw = StateDataFrame.fromAgentStateMap(
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
        timingsMap.put("timestep", Integer.toString(this.executor.getCurrentTimeStep()));
        timingsMap.put("pansim", this.stepEnd > -1 ? Long.toString(millis - stepEnd) : "");

        // Cannot be parallelized
        StateDataFrame state_output_df = new StateDataFrame(cur_state_df_raw, allocator);
        timingsMap.put("stateExtracted", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        this.agentStateMap.fromDataframe(state_output_df, arguments.getThreads(), executor);
        timingsMap.put("stateProcessed", Long.toString(System.currentTimeMillis() - millis));
        state_output_df.close();
        millis = System.currentTimeMillis();

        // Cannot be parallelized
        VisitResultDataFrame visit_output_df = new VisitResultDataFrame(visit_output_df_raw, allocator);
        timingsMap.put("visitsExtracted", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        process_visit_output(visit_output_df);
        timingsMap.put("visitsProcessed", Long.toString(System.currentTimeMillis() - millis));
        visit_output_df.close();
        millis = System.currentTimeMillis();

        LOGGER.log(Level.FINE, String.format(
                "Received new state dataframe with %d rows", this.agentStateMap.getNumberOfStates()));
        LOGGER.log(Level.FINE, String.format(
                "Received new visit output dataframe with %d rows", visit_output_df.getSchemaRoot().getRowCount()));

        processStepStarting(this.executor.getCurrentTimeStep());
        timingsMap.put("stepStarting", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        List<Future<DeliberationResult<CandidateActivity>>> agentActions = ((NoRescheduleBlockingStepExecutor<CandidateActivity>) this.executor).doTimeStep(timingsMap);
        millis = System.currentTimeMillis();

        // Prepare results for pansim and misuse loop to set disease state on activities
        VisitDataFrame visitDf = VisitDataFrame.fromAgentActions(agentActions, this.agentStateMap, this.allocator);
        this.next_visit_df_raw = visitDf.toBytes();
        visitDf.close();
        timingsMap.put("visitsEncoded", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        StateDataFrame stateDf = StateDataFrame.fromAgentStateMap(this.agentStateMap.getAllAgentStates(), this.allocator);
        this.next_state_df_raw = stateDf.toBytes();
        stateDf.close();
        timingsMap.put("stateEncoded", Long.toString(System.currentTimeMillis() - millis));


        if(arguments.saveStateDataFrames()) {
            write_state_dataframes(STATE_DATAFRAME_DIR_NAME, cur_state_df_raw, next_state_df_raw);
        }

        if(arguments.saveVisitsDataFrames()) {
            write_state_dataframes(VISITS_DATAFRAME_DIR_NAME, visit_output_df_raw, next_visit_df_raw);
        }

        millis = System.currentTimeMillis();
        // TODO don't parallelize, as we suppress calculations anyway when doing scaling?
        processStepFinished(this.executor.getCurrentTimeStep() - 1, this.executor.getLastTimeStepDuration(), agentActions);
        timingsMap.put("stepFinished", Long.toString(System.currentTimeMillis() - millis));

        this.timingsTracker.writeKeyMapToFile(
                arguments.getStartdate().plusDays(this.executor.getCurrentTimeStep()),
                timingsMap
        );

        this.stepEnd = System.currentTimeMillis();
    }

    public byte[] getNextStateDataFrame() {
        LOGGER.log(Level.FINE, String.format(
                "Returning state dataframe for time step %d", this.executor.getCurrentTimeStep()));
        return next_state_df_raw;
    }

    public byte[] getNextVisitDataFrame() {
        LOGGER.log(Level.FINE, String.format(
                "Returning next visit dataframe for time step %d", this.executor.getCurrentTimeStep()));
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
        processSimulationFinishedHook(this.executor.getCurrentTimeStep(), this.executor.getLastTimeStepDuration());
        executor.shutdown();
        return true;
    }

    private void process_visit_output(VisitResultDataFrame visit_output_df) {
        List<Callable<Void>> runnables = new ArrayList<>();
        for(int i = 0; i < this.arguments.getThreads(); i++) {
            runnables.add(new ProcessVisitOutput(observationNotifier, visit_output_df, i, arguments.getThreads(), executor.getCurrentTimeStep()));
        }

        try {
            this.executor.useExecutorForTasks(runnables);
        } catch (InterruptedException e) {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Failed to process time step visits concurrently. Exiting");
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(10);
        }
    }

    static class ProcessVisitOutput implements Callable<Void> {

        ObservationNotifier notifier;
        VisitResultDataFrame dataFrame;
        private final int thread;
        private final int threads_total;
        int currentTimeStep;

        public ProcessVisitOutput(ObservationNotifier notifier, VisitResultDataFrame dataFrame, int i, int threads, int currentTimeStep) {
            this.notifier = notifier;
            this.dataFrame = dataFrame;
            this.currentTimeStep = currentTimeStep;
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
                this.notifier.notifyVisit(visit.getPersonID(), currentTimeStep, visit);
            }
            return null;
        }
    }

    @Override
    public boolean start() {
        runFirstTimeStep();
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

    private String prepare_output(String dir) {
        File outputDir = Paths.get(arguments.getOutputDir().getAbsolutePath(), dir).toFile();
        return arguments.getOutputDir().getAbsolutePath();
    }

    private void write_state_dataframe_to_file(byte[] data_frame, File out) {
        if (!Methods.createOutputFile(out)) return;

        try(FileOutputStream fos = new FileOutputStream(out)) {
            fos.write(data_frame);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write data frame to file " + out.getAbsolutePath());
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private void write_state_dataframes(String subDir, byte[] current_state_df_raw, byte[] next_state_df_raw) {
        File fout_pansim = Paths.get(this.parentDir, subDir, String.format("%s_timestep_%03d_pansim.raw", subDir, this.executor.getCurrentTimeStep())).toFile();
        File fout_behavior = Paths.get(this.parentDir, subDir, String.format("%s_timestep_%03d_behavior.raw", subDir, this.executor.getCurrentTimeStep())).toFile();

        write_state_dataframe_to_file(current_state_df_raw, fout_pansim);
        write_state_dataframe_to_file(next_state_df_raw, fout_behavior);
    }
}
