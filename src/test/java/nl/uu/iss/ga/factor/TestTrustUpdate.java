package nl.uu.iss.ga.factor;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.iss.ga.mock.MockActivity;
import nl.uu.iss.ga.mock.MockAgent;
import nl.uu.iss.ga.mock.MockLocationHistoryContext;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.model.norm.modal.AllowWearMaskNorm;
import nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import nl.uu.iss.ga.model.norm.modal.WearMaskPublicIndoorsNorm;
import nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import nl.uu.iss.ga.model.norm.nonregimented.StayHomeNorm;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.simulation.agent.context.TrackPlansContext;
import nl.uu.iss.ga.simulation.agent.plan.AdjustHAITrustPlan;
import org.junit.jupiter.api.RepeatedTest;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TestTrustUpdate {

    public static final int MIN_NORMS = 3;
    public static final int MAX_NORMS = 10;

    // Norms from which we can get factors
    private static final List<Norm> normList = Arrays.asList(
            new AllowWearMaskNorm(),
            new MaintainDistanceNorm(),
            new WearMaskPublicIndoorsNorm(),
            new EncourageTeleworkNorm(),
            new IfSickStayHomeNorm(),
            new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.NONE, 10),
            new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.NONE, 50),
            new KeepGroupsSmallNorm(KeepGroupsSmallNorm.APPLIES.NONE, 100),
            new StayHomeNorm()
    );

    Random random = new Random();

    @RepeatedTest(100)
    void testTrustMonotonicity() {
        // Monotonicity: Positive evidence increases trust, negative evidence decreases trust
        // Translated to threshold/probability values: Evidence with higher value than current trust increases trust,
        // evidence with lower value than current trust decreases trust

        double discountFactor;
        do {
            discountFactor = random.nextDouble() / 2; // TOO high discount factor makes no sense
        } while (discountFactor == 0);

        double trust = random.nextDouble();

        LocationHistoryContext locationHistoryContext = getLocationHistoryContext();
        TrackPlansContext trackPlansContext = getTrackPlansContext();

        Agent<CandidateActivity> agent = MockAgent.createAgent(trust, Designation.none, locationHistoryContext, trackPlansContext);

        AdjustHAITrustPlan plan = new AdjustHAITrustPlan(discountFactor);

        List<Double> values = AdjustHAITrustPlan.getFactorValues(trackPlansContext, locationHistoryContext);
        double average = AdjustHAITrustPlan.average(values);

        try {
            plan.executeOnce(new PlanToAgentInterface<>(agent));
        } catch (PlanExecutionError e) {
            e.printStackTrace();
        }

        assertEquals(average < trust, MockAgent.getTrust(agent) < trust);
    }

    private LocationHistoryContext getLocationHistoryContext() {
        int seen = random.nextInt(Integer.MAX_VALUE);
        int symptomatic = random.nextInt(seen / 2);
        int behavior = random.nextInt(seen);
        return MockLocationHistoryContext.getLocationHistoryContext(seen, symptomatic, behavior);
    }

    private TrackPlansContext getTrackPlansContext() {
        TrackPlansContext context = new TrackPlansContext();
        int n = (int) Math.round(random.nextDouble() * (MAX_NORMS - MIN_NORMS) + MIN_NORMS);
        for(int i = 0; i < n; i++) {
            context.addLocation(MockActivity.getMockCandidateActivity(), sampleNorms());
        }
        return context;
    }

    private List<Norm> sampleNorms() {
        int n = (int) Math.round(random.nextDouble() * (MAX_NORMS - MIN_NORMS) + MIN_NORMS);
        List<Norm> appliedNormList = new ArrayList<>();
        for(int i = 0; i < n; i++) {
            int index = (int) Math.round(random.nextDouble()) * (normList.size() - 1);
            appliedNormList.add(normList.get(index));
        }
        return appliedNormList;
    }

}
