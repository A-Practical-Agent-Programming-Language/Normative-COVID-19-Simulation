package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.mock.MockSimulationArguments;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisplayName("Norm: Encourage Telework")
public class TestEncourageTeleworkNorm extends TestNonRegimentedNorm {

    @BeforeAll
    void beforeAll() {
        MockSimulationArguments.ensureInstance(false);
        norm = new EncourageTeleworkNorm();
    }

    @RepeatedTest(100)
    void testIncreasedTrustDecreasesAttitude() {
        testOnceIncreasedTrustDecreasesAttitude();
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

}
