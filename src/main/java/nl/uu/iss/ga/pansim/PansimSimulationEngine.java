package main.java.nl.uu.iss.ga.pansim;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
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
import java.util.HashMap;
import java.util.List;
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
    }

    private void runFirstTick() {
        try {
            byte[] first_state_df_raw = StateDataFrame.fromAgentStateMap(this.agentStateMap.getAllAgentStates(), this.allocator).toBytes();
            byte[] null_visit_output_raw = new VisitResultDataFrame(this.allocator).toBytes();
            this.runBehaviorModel(first_state_df_raw, null_visit_output_raw);
            LOGGER.log(Level.INFO, "Pansim Behavior Server Started");
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to run first iteration of the behavior model", e);
            System.exit(3);
        }
    }

    public void runBehaviorModel(byte[] cur_state_df_raw, byte[] visit_output_df_raw) throws IOException {
        this.agentStateMap.fromDataFrame(new StateDataFrame(cur_state_df_raw, allocator));
        VisitResultDataFrame visit_output_df = new VisitResultDataFrame(visit_output_df_raw, allocator);
        process_visit_output(visit_output_df);

        LOGGER.log(Level.FINE, String.format(
                "Received new state dataframe with %d rows", this.agentStateMap.getNumberOfStates()));
        LOGGER.log(Level.FINE, String.format(
                "Received new visit output dataframe with %d rows", visit_output_df.getSchemaRoot().getRowCount()));

        processTickPreHooks(this.executor.getCurrentTick());
        HashMap<AgentID, List<CandidateActivity>> agentActions = this.executor.doTick();

        // Prepare results for pansim and misuse loop to set disease state on activities
        this.next_visit_df_raw = VisitDataFrame.fromAgentActions(agentActions, this.agentStateMap, allocator).toBytes();
        this.next_state_df_raw = StateDataFrame.fromAgentStateMap(this.agentStateMap.getAllAgentStates(), this.allocator).toBytes();

        if(arguments.saveStateDataFrames()) {
            write_state_dataframes(cur_state_df_raw, next_state_df_raw);
        }

        processTickPostHook(this.executor.getCurrentTick() - 1, this.executor.getLastTickDuration(), agentActions);
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

    private void process_visit_output(VisitResultDataFrame visit_output_df) {
        for(int i = 0; i < visit_output_df.getSchemaRoot().getRowCount(); i++) {
            LocationHistoryContext.Visit visit = visit_output_df.getAgentVisit(i);
            this.observationNotifier.notifyVisit(visit.getPersonID(), this.executor.getCurrentTick(), visit);
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
