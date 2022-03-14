package nl.uu.iss.ga.dataframe;

import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.ActivityTime;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.pansim.visit.VisitDataFrame;
import nl.uu.iss.ga.pansim.visit.VisitResultDataFrame;
import org.junit.Ignore;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * The idea of this class it to test if the serialization and deserialization of visit data frames happens correctly,
 * but the in data has a different structure than the out data, so we cannot use the same approach as used for the
 * state data frames.
 *
 * Maybe later?
 */
public class TestVisitDataFrame extends TestDataFrame {

    private static final int N_ACTIONS_PER_AGENT_MIN = 2;
    private static  final int N_ACTIONS_PER_AGENT_MAX = 30;

//    @DisplayName("If we serialize a data frame, deserializing it should result in the exact same data frame")
//    @ParameterizedTest(name = "Using {0} threads")
//    @ValueSource(ints = {1, 2, 4, 8, 10, 16, 20})
//    @Ignore
    void testWriteRead(int threads) {
        List<Future<DeliberationResult<CandidateActivity>>> agentActions = mockAgentActivities();
        AgentStateMap agentStateMap = createAgentStateMap(TEST_WITH_N_AGENTS, random.nextLong());
        VisitDataFrame visitDfSource = VisitDataFrame.fromAgentActions(agentActions, agentStateMap, this.allocator);

        try {
            byte[] bytes = visitDfSource.toBytes();
            System.out.println(bytes);
            VisitResultDataFrame visitResultDataFrame = new VisitResultDataFrame(bytes, allocator);
            System.out.println(visitResultDataFrame);
        } catch (IOException e) {
            e.printStackTrace();
        }


        System.out.println(agentActions.size());
    }

    private List<Future<DeliberationResult<CandidateActivity>>> mockAgentActivities() {
        List<Future<DeliberationResult<CandidateActivity>>> actions = new ArrayList<>();

        for(int i = 0; i < TEST_WITH_N_AGENTS; i++) {
            try {
                actions.add(generateActionsForAgent(i));
            } catch (ExecutionException | InterruptedException e) {
                e.printStackTrace();
            }
        }

        return actions;
    }

    private Future<DeliberationResult<CandidateActivity>> generateActionsForAgent(long pid) throws ExecutionException, InterruptedException {
        int actions = (int) Math.round((N_ACTIONS_PER_AGENT_MAX - N_ACTIONS_PER_AGENT_MIN) * random.nextDouble() + N_ACTIONS_PER_AGENT_MIN);
        List<CandidateActivity> activities = new ArrayList<>();
        for(int i = 0; i < actions; i++) {
            Activity activity = new Activity(
                    random.nextLong(),
                    random.nextLong(),
                    random.nextInt(),
                    getRandomEnumValue(ActivityType.class, random),
                    getRandomEnumValue(DetailedActivity.class, random),
                    getRandomActivityTime(),
                    random.nextInt()
            );
            setRandomLocation(activity);
            activities.add(new CandidateActivity(activity));
        }
        return resultToFuture(new DeliberationResult<>(getAgentForID(pid), activities));
    }

    private Future<DeliberationResult<CandidateActivity>> resultToFuture(
            DeliberationResult<CandidateActivity> deliberationResult
    ) throws ExecutionException, InterruptedException {
        Future<DeliberationResult<CandidateActivity>> future = mock(Future.class);
        when(future.get()).thenReturn(deliberationResult);
        return future;
    }

    private ActivityTime getRandomActivityTime() {
        return new ActivityTime(random.nextInt(60 * 60 * 24 * 7));
    }

    private void setRandomLocation(Activity activity) {
        LocationEntry locationEntry = mock(LocationEntry.class);
        when(locationEntry.getLocationID()).thenReturn(random.nextLong());
        activity.setLocation(locationEntry);
    }


}
