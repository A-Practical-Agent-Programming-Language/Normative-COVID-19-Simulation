package main.java.nl.uu.iss.ga.pansim;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import main.java.nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.environment.AgentStateMap;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
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

    private final GatewayServer gatewayServer;

    private final AgentStateMap agentStateMap;
    private final ObservationNotifier observationNotifier;

    private final TickExecutor<CandidateActivity> executor;

    private byte[] next_state_df_raw;
    private byte[] next_visit_df_raw;


    public RootAllocator allocator = new RootAllocator(Long.MAX_VALUE);

    public PansimSimulationEngine(Platform platform, ObservationNotifier observationNotifier, AgentStateMap agentStateMap, TickHookProcessor<CandidateActivity>... processors) {
        super(platform, processors);
        this.gatewayServer = new GatewayServer(this);
        this.agentStateMap = agentStateMap;
        this.observationNotifier = observationNotifier;
        gatewayServer.start();
        this.executor = platform.getTickExecutor();
    }

    private void runFirstTick() {
        try {
            byte[] first_state_df_raw = StateDataFrame.fromAgentStateMap(this.agentStateMap.getAllAgentStates(), this.allocator).toBytes();
            byte[] null_visit_output_raw = new VisitResultDataFrame(this.allocator).toBytes();
            this.runBehaviorModel(first_state_df_raw, null_visit_output_raw);
            System.out.println("Pansim Behavior Server Started");
        } catch (IOException e) {
            System.err.println("Running first state already failed");
            e.printStackTrace();
            System.exit(3);
        }
    }

    public void runBehaviorModel(byte[] cur_state_df_raw, byte[] visit_output_df_raw) throws IOException {
        this.agentStateMap.fromDataFrame(new StateDataFrame(cur_state_df_raw, allocator));
        VisitResultDataFrame visit_output_df = new VisitResultDataFrame(visit_output_df_raw, allocator);
        process_visit_output(visit_output_df);

        System.out.printf("Received new state dataframe with %d rows\n", this.agentStateMap.getNumberOfStates());
        System.out.printf("Received new visit output dataframe with %d rows\n", visit_output_df.getSchemaRoot().getRowCount());

        processTickPreHooks(this.executor.getCurrentTick());
        HashMap<AgentID, List<CandidateActivity>> agentActions = this.executor.doTick();
        processTickPostHook(this.executor.getCurrentTick(), this.executor.getLastTickDuration(), agentActions);

        // Prepare results for pansim
        this.next_visit_df_raw = VisitDataFrame.fromAgentActions(agentActions, allocator).toBytes();
        this.next_state_df_raw = StateDataFrame.fromAgentStateMap(this.agentStateMap.getAllAgentStates(), this.allocator).toBytes();
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

    private void process_visit_output(VisitResultDataFrame visit_output_df) {
        for(int i = 0; i < visit_output_df.getSchemaRoot().getRowCount(); i++) {
            LocationHistoryContext.Visit visit = visit_output_df.getAgentVisit(i);
            this.observationNotifier.notifyVisit(visit.getPersonID(), this.executor.getCurrentTick(), visit);
        }
    }

    @Override
    public boolean start() {
        runFirstTick();
        return true;
    }
}
