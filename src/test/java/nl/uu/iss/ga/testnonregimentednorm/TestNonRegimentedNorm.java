package nl.uu.iss.ga.testnonregimentednorm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.mock.MockActivity;
import nl.uu.iss.ga.mock.MockAgent;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;

@TestInstance(value = TestInstance.Lifecycle.PER_CLASS)
public class TestNonRegimentedNorm {
    protected final Random random = new Random();
    protected final Activity activity = MockActivity.getMockActivity();

    NonRegimentedNorm norm;
    Agent<CandidateActivity> agent;
    LocationHistoryContext context;

    @BeforeAll
    void beforeEachMaster() {
        agent = MockAgent.createAgent(0, Designation.none);
        context = MockLocationHistoryContext.getLocationHistoryContext(true);
        MockAgent.addContext(agent, context);
    }

    void testOnceIncreasedTrustDecreasesAttitude() {
        double t1 = random.nextDouble();
        double t2 = random.nextDouble();

        MockAgent.setTrust(agent, t1);
        assertEquals(t1, MockAgent.getTrust(agent));
        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);
        MockAgent.setTrust(agent, t2);
        assertEquals(t2, MockAgent.getTrust(agent));
        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(t1 > t2, a1 < a2);
    }

    void testOnceMoreMasksSeenDecreasesAttitude() {
        double trust = random.nextDouble();

        MockAgent.setTrust(agent, trust);

        double fractionMask1 = random.nextDouble();
        double fractionMask2 = random.nextDouble();

        when(context.getLastDaysFractionMaskAt(anyInt(), anyLong())).thenReturn(fractionMask1);
        when(context.getLastDaysFractionMask(anyInt())).thenReturn(fractionMask1);

        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        when(context.getLastDaysFractionMaskAt(anyInt(), anyLong())).thenReturn(fractionMask2);
        when(context.getLastDaysFractionMask(anyInt())).thenReturn(fractionMask2);

        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(fractionMask1 > fractionMask2, a1 < a2);
    }

    void testOnceMoreDistancingSeenDecreasesAttitude() {
        double trust = random.nextDouble();

        MockAgent.setTrust(agent, trust);

        double fractionDistancing1 = random.nextDouble();
        double fractionDistancing2 = random.nextDouble();

        when(context.getLastDaysFractionDistancingAt(anyInt(), anyLong())).thenReturn(fractionDistancing1);
        when(context.getLastDaysFractionDistancing(anyInt())).thenReturn(fractionDistancing1);

        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        when(context.getLastDaysFractionDistancingAt(anyInt(), anyLong())).thenReturn(fractionDistancing2);
        when(context.getLastDaysFractionDistancing(anyInt())).thenReturn(fractionDistancing2);

        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(fractionDistancing1 > fractionDistancing2, a1 < a2);
    }

    void testOnceMoreSymptomaticSeenIncreasesAttitude() {
        testOnceMoreSymptomaticSeenChangesAttitude(true);
    }

    void testOnceMoreSymptomaticSeenDecreasesAttitude() {
        testOnceMoreSymptomaticSeenChangesAttitude(false);
    }

    private void testOnceMoreSymptomaticSeenChangesAttitude(boolean attitudeIncreases) {
        double trust = random.nextDouble();

        MockAgent.setTrust(agent, trust);

        double fractionSymptomatic1 = random.nextDouble();
        double fractionSymptomatic2 = random.nextDouble();

        when(context.getLastDaysFractionSymptomaticAt(anyInt(), anyLong())).thenReturn(fractionSymptomatic1);
        when(context.getLastDaysFractionSymptomatic(anyInt())).thenReturn(fractionSymptomatic1);

        double a1 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        when(context.getLastDaysFractionSymptomaticAt(anyInt(), anyLong())).thenReturn(fractionSymptomatic2);
        when(context.getLastDaysFractionSymptomatic(anyInt())).thenReturn(fractionSymptomatic2);

        double a2 = norm.calculateAttitude(new AgentContextInterface<>(agent), activity);

        assertEquals(
                fractionSymptomatic1 >= fractionSymptomatic2,
                attitudeIncreases ? Double.compare(a1, a2) >= 0 : Double.compare(a1, a2) <= 0
        );
    }
}
