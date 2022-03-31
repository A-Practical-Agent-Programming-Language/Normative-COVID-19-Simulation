package nl.uu.iss.ga.factor;

import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.model.factor.FractionMask;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.model.factor.NOverLimit;
import nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.testnonregimentednorm.TestNonRegimentedNorm;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;

public class TestFactor  {

    Random random = new Random();

    private void testValueForContext1HigherThanForContext2(
            IFactor factor,
            LocationHistoryContext context1,
            LocationHistoryContext context2,
            int comparingValue1,
            int comparingValue2,
            String comparingValueName
    ) {
            double v1 = factor.calculateValue(anyInt(), context1);
            double v2 = factor.calculateValue(anyInt(), context2);

            int lowInfected = Math.min(comparingValue1, comparingValue2);
            int highInfected = Math.max(comparingValue1, comparingValue2);
            double vExpectedHigh = comparingValue1 > comparingValue2 ? v1 : v2;
            double vExpectedLow = comparingValue1 > comparingValue2 ? v2 : v1;

            assertTrue(vExpectedHigh > vExpectedLow || (vExpectedHigh == vExpectedLow && vExpectedHigh == 1),
                    String.format(
                            "Expected lower value for %2$f %1$s than for %3$f %1$s. Got %4$f vs %5$f",
                            comparingValueName,
                            (double) highInfected / context1.getLastDaySeen(),
                            (double) lowInfected / context1.getLastDaySeen(),
                            vExpectedLow, vExpectedHigh
                    ));
    }

    private void testValueForContext1LowerThanForContext2(
            IFactor factor,
            LocationHistoryContext context1,
            LocationHistoryContext context2,
            int comparingValue1,
            int comparingValue2,
            String comparingValueName
    ) {
        testValueForContext1HigherThanForContext2(
                factor,
                context2, context1,
                comparingValue2, comparingValue1,
                comparingValueName
        );
    }

    @Nested
    class TestAccomodatedToWorkFromHome {
        // TODO if we actually implement some code here
    }

    @Nested
    class TestFractionDistance extends TestNonRegimentedNorm {

        @RepeatedTest(100)
        void testMoreDistancingIncreasesTrust() {
            int seen = random.nextInt(Integer.MAX_VALUE);
            int behavior1 = random.nextInt(seen);
            int behavior2 = random.nextInt(seen);
            int infected = random.nextInt(seen/2);

            LocationHistoryContext locationHistoryContext1 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected, behavior1);
            LocationHistoryContext locationHistoryContext2 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected, behavior2);

            testValueForContext1HigherThanForContext2(
                    new FractionMask(),
                    locationHistoryContext1, locationHistoryContext2,
                    behavior1, behavior2,
                    "distancing"
            );
        }

    }

    @Nested
    class TestFractionMask {

        @RepeatedTest(100)
        void testMoreMaskIncreasesTrust() {
            int seen = random.nextInt(Integer.MAX_VALUE);
            int behavior1 = random.nextInt(seen);
            int behavior2 = random.nextInt(seen);
            int infected = random.nextInt(seen/2);

            LocationHistoryContext locationHistoryContext1 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected, behavior1);
            LocationHistoryContext locationHistoryContext2 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected, behavior2);

            testValueForContext1HigherThanForContext2(
                    new FractionMask(),
                    locationHistoryContext1, locationHistoryContext2,
                    behavior1, behavior2,
                    "wearing masks"
            );
        }
    }

    @Nested
    class TestFractionSymptomatic {

        @RepeatedTest(100)
        void testMoreSymptomaticDecreasesTrust() {
            int seen = random.nextInt(Integer.MAX_VALUE);
            int behavior = random.nextInt(seen);
            int infected1 = random.nextInt(seen/2);
            int infected2 = random.nextInt(seen/2);

            LocationHistoryContext locationHistoryContext1 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected1, behavior);
            LocationHistoryContext locationHistoryContext2 = MockLocationHistoryContext.getLocationHistoryContext(seen, infected2, behavior);

            testValueForContext1LowerThanForContext2(
                    new FractionSymptomatic(),
                    locationHistoryContext1,
                    locationHistoryContext2,
                    infected1,
                    infected2,
                    "infected"
            );
        }
    }

    @Nested
    class TestNOverLimit {

        @ParameterizedTest(name = "Maximum group size = {0}")
        @ValueSource(ints = {10, 20, 50, 100})
        void testMoreOverLimitDecreasesTrust(int maxGroupSize) {
            for(int i = 0; i < maxGroupSize; i++) {
                for(int j = 1; j < maxGroupSize+1; j++) {
                    LocationHistoryContext c1 = MockLocationHistoryContext.getLocationHistoryContext(i+maxGroupSize, 0, 0);
                    LocationHistoryContext c2 = MockLocationHistoryContext.getLocationHistoryContext(j+maxGroupSize, 0, 0);

                    IFactor factor = new NOverLimit(maxGroupSize, KeepGroupsSmallNorm.CURVE_SLOPE_FACTOR);
                    double v1 = factor.calculateValue(0, c1);
                    double v2 = factor.calculateValue(0, c2);

                    if (i == j) {
                        assertEquals(v1, v2, String.format(
                                "Expected trust to be equal for %d agents seen over maximum. Got %f vs %f",
                                i, v1, v2)
                        );
                    } else {
                        assertTrue(i < j ? v1 > v2 : v2 > v1,
                                String.format(
                                        "Expected trust for %d agents over maximum seen to be %s than for " +
                                                "%d agents over maximum seen. Got %f vs %f",
                                        i, i < j ? "HIGHER": "LOWER", j, v1, v2
                                )
                        );
                    }
                }
            }
        }
    }
}