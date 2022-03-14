package nl.uu.iss.ga;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.mock.MockActivity;
import nl.uu.iss.ga.mock.MockAgent;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.model.norm.modal.AllowWearMaskNorm;
import nl.uu.iss.ga.model.norm.modal.EncourageDistanceNorm;
import nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import nl.uu.iss.ga.model.norm.modal.WearMaskPublicIndoorsNorm;
import nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import nl.uu.iss.ga.model.norm.nonregimented.StayHomeNorm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import org.junit.jupiter.api.*;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class TestNormReasoning {

    private Agent<CandidateActivity> agent;
    private GoalPlanScheme goalPlanScheme;
    private Activity activity;
    private Random random;
    private int seen;

    @BeforeEach
    void beforeEach() {
        activity = MockActivity.getMockActivity();
        random = new Random();
        seen = random.nextInt(Integer.MAX_VALUE);
        LocationHistoryContext locationHistoryContext =
                MockLocationHistoryContext.getLocationHistoryContext(seen, random.nextInt(seen), random.nextInt(seen));
        agent = MockAgent.createAgent(Math.random(), Designation.none, locationHistoryContext);
        Random mockedRandom = mock(Random.class);
        BeliefContext beliefContext = agent.getContext(BeliefContext.class);
        when(beliefContext.getRandom()).thenReturn(mockedRandom);
        when(mockedRandom.nextDouble()).thenAnswer(new Answer<Double>() {
            private double answer = 0d;
            @Override
            public Double answer(InvocationOnMock invocationOnMock) {
                answer += 0.001;
                return answer;
            }
        });
        this.goalPlanScheme = createGoalPlanScheme();
    }

    private GoalPlanScheme createGoalPlanScheme() {
        AgentContextInterface<CandidateActivity> agentContextInterface = new AgentContextInterface<>(agent);
        GoalPlanScheme goalPlanScheme = new GoalPlanScheme();
        try {
            Field field = goalPlanScheme.getClass().getDeclaredField("agentContextInterface");
            field.setAccessible(true);
            field.set(goalPlanScheme, agentContextInterface);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return goalPlanScheme;
    }


    void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(NonRegimentedNorm norm) {
        int totalIgnored = 0;
        for(int i = 0; i < 1000; i++) {
            try {
                Method method = goalPlanScheme.getClass().getDeclaredMethod("evaluateToIgnore", Norm.class, Activity.class);
                method.setAccessible(true);
                boolean ignore = (boolean) method.invoke(
                        goalPlanScheme, norm, activity
                );
                if (ignore) totalIgnored++;
            } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }

        double attitude = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(Math.round(attitude * 1000), totalIgnored, 1);
    }

    @Nested
    class AllowWearMask {

        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
           TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new AllowWearMaskNorm());
        }

    }

    @Nested
    class EncourageDistance {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new EncourageDistanceNorm());
        }
    }

    @Nested
    class MaintainDistance {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new MaintainDistanceNorm());
        }
    }

    @Nested
    class WearMaskPublIndoors {

        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new WearMaskPublicIndoorsNorm());
        }

    }

    @Nested
    class EncourageTelework {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new EncourageTeleworkNorm());
        }
    }

    @Nested
    class IfSickStayHome {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new IfSickStayHomeNorm());
        }
    }

    @Nested
    class KeepGroupsSmall {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitudeAppliesNone() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(
                    new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.NONE, (int) Math.round(seen + random.nextDouble() * 500))
            );
        }

        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitudeAppliesPrivate() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(
                    new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.PRIVATE, (int) Math.round(seen + random.nextDouble() * 500))
            );
        }

        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitudeAppliesPublic() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(
                    new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.PUBLIC, (int) Math.round(seen + random.nextDouble() * 500))
            );
        }

        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitudeAppliesAll() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(
                    new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.ALL, (int) Math.round(seen + random.nextDouble() * 500))
            );
        }
    }

    @Nested
    class StayHome {
        @RepeatedTest(10)
        void testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude() {
            TestNormReasoning.this.testEvaluateToIgnoreIgnoresWithProbabilityEqualToAttitude(new StayHomeNorm());
        }
    }
}
