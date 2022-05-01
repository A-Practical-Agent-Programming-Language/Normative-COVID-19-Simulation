package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.mock.MockAgent;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.mock.MockSimulationArguments;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import org.junit.jupiter.api.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

public class TestKeepGroupsSmallNorm extends TestNonRegimentedNorm {

    @BeforeEach
    void beforeEach() {
        MockSimulationArguments.ensureInstance(false);
        double trust = random.nextDouble();
        MockAgent.setTrust(agent, trust);
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        norm = new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.ALL, 10);
        testOnceIncreasedTrustDecreasesAttitude();
    }

    @RepeatedTest(100)
    void testMoreSeenDecreasesAttitude() {
        int seen1 = random.nextInt(Integer.MAX_VALUE);
        int seen2 = random.nextInt(Integer.MAX_VALUE);
        int symptomatic = random.nextInt(Math.min(seen1, seen2));

        // Ensure the number of observed agents is higher than what is allowed; otherwise this test does not make sense
        int allowed = random.nextInt(Math.min(seen1, seen2));

        KeepGroupsSmallNorm norm = new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.ALL, allowed);

        setSeen(seen1, symptomatic, (double) symptomatic / seen1);
        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        setSeen(seen2, symptomatic, (double) symptomatic / seen1);
        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(
                seen1 > seen2, a1 < a2,
                String.format(
                        "Expected attitude %f to be lower than %f for seen %d vs %d",
                        seen1 > seen2 ? a1 : a2,
                        seen1 > seen2 ? a2 : a1,
                        Math.max(seen1, seen2),
                        Math.min(seen1, seen2)
                )
        );
    }

    @RepeatedTest(100)
    void testNormDoesNotApplyIfPastAverageIsBelowAllowedMax() {
        int allowed = random.nextInt(Integer.MAX_VALUE);
        int seen = random.nextInt(allowed);
        int symptomatic = random.nextInt(seen);
        setSeen(seen, symptomatic);

        KeepGroupsSmallNorm norm = new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.ALL, allowed);

        assertFalse(norm.applicable(activity, new AgentContextInterface<>(agent)));
    }

    @RepeatedTest(100)
    void testMoreSymptomaticDecreasesAttitude() {
        int seen = random.nextInt(100) + 1; // Avoid none seen as it tells us nothing
        int allowed = random.nextInt(seen);

        int symptomatic1 = random.nextInt(Math.min(seen, (int) Math.floor(1d / FractionSymptomatic.ALPHA)));
        int symptomatic2 = random.nextInt(Math.min(seen, (int) Math.floor(1d / FractionSymptomatic.ALPHA)));

        KeepGroupsSmallNorm norm = new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.ALL, allowed);

        MockLocationHistoryContext.update(context, seen, symptomatic1, 0);
        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        MockLocationHistoryContext.update(context, seen, symptomatic2, 0);
        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(symptomatic1 > symptomatic2, a1 < a2, String.format(
                "%d (%f%%) symptomatic seen results in attitude %f, %d (%f%%) symptomatic seen results in attitude %f",
                symptomatic1, (double) symptomatic1 / seen, a1, symptomatic2, (double) symptomatic2 / seen, a2
        ));
    }

    @TestFactory
    Collection<DynamicTest> generateAppliesDoesNotChangeAttitude() {
        List<DynamicTest> tests = new ArrayList<>();
        for(int i = 0; i < 100; i++) {
            int seen = random.nextInt(Integer.MAX_VALUE);
            int allowed = random.nextInt(seen);
            int symptomatic = random.nextInt(seen);
            setSeen(seen, symptomatic);

            KeepGroupsSmallNorm norm = new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.NONE, allowed);
            double attitude = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

            for (KeepGroupsSmallNorm.APPLIES appliesTo : KeepGroupsSmallNorm.APPLIES.values()) {
                if (!KeepGroupsSmallNorm.APPLIES.NONE.equals(appliesTo)) {
                    tests.add(DynamicTest.dynamicTest(
                            String.format(
                                    "Repetition %d of %d: %s", i, 100, appliesTo
                            ),
                            () -> {
                                KeepGroupsSmallNorm norm2 = new KeepGroupsSmallNorm(appliesTo, allowed);
                                setSeen(seen, symptomatic);
                                double a2 = norm2.calculateAttitude(new AgentContextInterface<>(agent), activity);
                                assertEquals(attitude, a2, 0.000001d);
                            }
                    ));
                }
            }
        }

        return tests;
    }

    private void setSeen(int seen, int symptomatic) {
        setSeen(seen, symptomatic, (double) symptomatic / seen);
    }

    private void setSeen(int seen, int symptomatic, double fractionSymptomatic) {
        when(context.getLastDaySeen()).thenReturn(seen);
        when(context.getLastDaySeenAt(anyLong())).thenReturn(seen);
        when(context.getLastDaysSeen(anyInt())).thenReturn(seen);
        when(context.getLastDaysSeenAt(anyInt(), anyLong())).thenReturn(seen);
        when(context.getLastDaysSeenAverage(anyInt())).thenReturn((double) seen);
        when(context.getLastDaysSeenAtAverage(anyInt(), anyLong())).thenReturn((double) seen);

        when(context.getLastDaySeenSymptomatic()).thenReturn(symptomatic);
        when(context.getLastDaysSeenSymptomatic(anyInt())).thenReturn(symptomatic);
        when(context.getLastDaySeenSymptomaticAt(anyLong())).thenReturn(symptomatic);
        when(context.getLastDaysSeenSymptomaticAt(anyInt(), anyLong())).thenReturn(symptomatic);

        when(context.getLastDaysSeenSymptomaticAverage(anyInt())).thenReturn(fractionSymptomatic);
        when(context.getLastDaysSeenSymptomaticAverageAt(anyInt(), anyLong())).thenReturn(fractionSymptomatic);

        when(context.getLastDayFractionSymptomatic()).thenReturn(fractionSymptomatic);
        when(context.getLastDaysFractionSymptomatic(anyInt())).thenReturn(fractionSymptomatic);
        when(context.getLastDayFractionSymptomaticAt(anyLong())).thenReturn(fractionSymptomatic);
        when(context.getLastDaysFractionDistancingAt(anyInt(), anyLong())).thenReturn(fractionSymptomatic);
    }

}
