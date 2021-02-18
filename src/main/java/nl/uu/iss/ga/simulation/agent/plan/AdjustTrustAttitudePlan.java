package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.AdjustTrustAttitudeGoal;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class AdjustTrustAttitudePlan extends RunOncePlan<CandidateActivity> {

    private final AdjustTrustAttitudeGoal goal;

    public AdjustTrustAttitudePlan(AdjustTrustAttitudeGoal goal) {
        this.goal = goal;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        BeliefContext context = planToAgentInterface.getContext(BeliefContext.class);
        double newTrust = context.getPriorTrustAttitude();
        newTrust -= this.goal.getFatigueFactor();
        if (newTrust < 0)
            newTrust = 0;
        context.setPriorTrustAttitude(newTrust);
        return null;
    }
}
