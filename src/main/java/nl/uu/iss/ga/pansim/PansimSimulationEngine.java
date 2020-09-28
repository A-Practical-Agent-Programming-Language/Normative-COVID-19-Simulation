package main.java.nl.uu.iss.ga.pansim;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import main.java.nl.uu.iss.ga.simulation.environment.AgentStateMap;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.AbstractSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;
import org.apache.arrow.memory.RootAllocator;
import py4j.GatewayServer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;

public class PansimSimulationEngine extends AbstractSimulationEngine<CandidateActivity> {

    private final AgentStateMap agentStateMap;
    private final GatewayServer gatewayServer;

    private byte[] next_state_df_raw;
    private byte[] next_visit_df_raw;

    private TickExecutor<CandidateActivity> executor;

    public String startStateFile;
    public RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);
    private AgentStateMap start_state_df;

    public PansimSimulationEngine(Platform platform, AgentStateMap agentStateMap, TickHookProcessor<CandidateActivity>... processors) {
        super(platform, processors);
        this.gatewayServer = new GatewayServer(this);
        this.agentStateMap = agentStateMap;
        gatewayServer.start();
        System.out.println("Pansim Behavior Server Started");
    }

    public void runBehaviorModel(byte[] cur_state_df_raw, byte[] visit_output_df_raw) throws IOException {
        StateDataFrame cur_state_df = new StateDataFrame(cur_state_df_raw, allocator);
        VisitResultDataFrame visit_output_df = new VisitResultDataFrame(visit_output_df_raw, allocator);

        System.out.printf("Recived new state dataframe with %d rows\n", cur_state_df.getSchemaRoot().getRowCount());
        System.out.printf("Recived new visit output dataframe with %d rows\n", visit_output_df.getSchemaRoot().getRowCount());

        this.start_state_df.fromDataFrame(cur_state_df);

        processTickPreHooks(this.executor.getCurrentTick());
        HashMap<AgentID, List<CandidateActivity>> agentActions = this.executor.doTick();
        processTickPostHook(this.executor.getCurrentTick(), this.executor.getLastTickDuration(), agentActions);
        this.next_visit_df_raw = VisitDataFrame.fromAgentActions(agentActions, allocator).toBytes();
        this.next_state_df_raw = cur_state_df_raw;
    }

    public byte[] getNextStateDataFrame() {
        System.out.printf("Returning state dataframe for tick %d%n", this.executor.getCurrentTick());
        return next_state_df_raw;
    }

    public byte[] getNextVisitDataFrame() {
        System.out.printf("Returning next visit dataframe for tick %d%n", this.executor.getCurrentTick());
        return next_visit_df_raw;
    }

    public void shutdown() {
        if (this.gatewayServer != null) {
            this.gatewayServer.shutdown();
            System.out.println("Pansim Behavior Server Shutdown");
        }
        processSimulationFinishedHook(this.executor.getCurrentTick(), this.executor.getLastTickDuration());
        System.exit(0);
    }

    @Override
    public boolean start() {
        return true;
    }
}
