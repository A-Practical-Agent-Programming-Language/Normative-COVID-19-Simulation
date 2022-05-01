package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.mock.MockAgent;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.mock.MockSimulationArguments;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import nl.uu.iss.ga.util.config.SimulationArguments;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("Norm: If Sick Stay Home")
public class TestIfSickStayHomeNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        norm = new IfSickStayHomeNorm();
        MockSimulationArguments.ensureInstance(false);
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
    }

    @TestFactory
    void testIncreasedInfectionsIncreasesAttitude() {

    }

    @RepeatedTest(100)
    void testIncreasedFractionSymptomaticIncreasesAttitude() {
        double trust = random.nextDouble();
        MockAgent.setTrust(agent, trust);
        int s1 = random.nextInt((int) Math.floor(1d / FractionSymptomatic.ALPHA));
        int s2 = random.nextInt((int) Math.floor(1d / FractionSymptomatic.ALPHA));

        int sMax = Math.max(s1, s2);
        int seen = (int) (sMax + random.nextDouble() * ((sMax * 10) - sMax));

        MockLocationHistoryContext.update(context, seen, s1, 0);
        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        MockLocationHistoryContext.update(context, seen, s2, 0);
        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(s1 > s2, a1 > a2,
                String.format(
                        "%d symptomatic expected to increase attitude %f compared to %d symptomatic. Got %f",
                        s1, a1, s2, a2
                )
        );
    }
}
