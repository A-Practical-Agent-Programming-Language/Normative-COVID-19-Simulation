package nl.uu.iss.ga.dataframe;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.step.DefaultBlockingStepExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.step.StepExecutor;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.pansim.state.AgentState;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.pansim.state.StateDataFrame;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

public class TestStateDataFrame extends TestDataFrame {

    @DisplayName("If we serialize a data frame, deserializing it should result in the exact same data frame")
    @ParameterizedTest(name = "Using {0} threads")
    @ValueSource(ints = {1, 2, 4, 8, 10, 16, 20})
    void testWriteRead(int threads) {
        long seed = random.nextLong();
        AgentStateMap sourceStateMap = createAgentStateMap(TEST_WITH_N_AGENTS, seed);
        AgentStateMap targetStateMap = createAgentStateMap(TEST_WITH_N_AGENTS, seed);

        StateDataFrame df_written = StateDataFrame.fromAgentStateMap(sourceStateMap.getAllAgentStates(), allocator);
        byte[] bytes = new byte[0];
        try {
            bytes = df_written.toBytes();
        } catch (IOException e) {
            fail("Failed to serialize state data frame", e);
        }

        StateDataFrame df_read;
        try {
            df_read = new StateDataFrame(bytes, allocator);
            StepExecutor<CandidateActivity> StepExecutor = new DefaultBlockingStepExecutor<>(threads, new Random(seed));
            targetStateMap.fromDataframe(df_read, threads, StepExecutor);

            compareAgentStateMaps(sourceStateMap, targetStateMap);

        } catch (IOException e) {
            fail("Failed to deserialize state data frame", e);
        }
    }

    private void compareAgentStateMaps(AgentStateMap written, AgentStateMap read) {
        assertEquals(written.getNumberOfStates(), read.getNumberOfStates(),
                "Number of states in written and read maps differ");

        assertEquals(written.getPidToAgentMap().size(), read.getPidToAgentMap().size());

        for(AgentID agentID : written.getAgentToPidMap().keySet()) {
            assertEquals(
                    written.getAgentToPidMap().get(agentID),
                    read.getAgentToPidMap().get(agentID),
                    String.format(
                            "Different PID for agent ID %s: Expected %d, got %d",
                            agentID,
                            written.getAgentToPidMap().get(agentID),
                            read.getAgentToPidMap().get(agentID)
                    )
            );

            AgentState stateW = written.getAgentState(agentID);
            AgentState stateR  = read.getAgentState(agentID);

            assertEquals(stateW.getPid(), stateR.getPid());
            assertEquals(stateW.getState(), stateR.getState());
            assertEquals(stateW.getNextState(), stateR.getNextState());
            assertEquals(stateW.getGroup(), stateR.getGroup());
            assertEquals(stateW.getDwell_time(), stateR.getDwell_time());
        }
    }

}
