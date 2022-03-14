package nl.uu.iss.ga.simulation.agent.plan.norm;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.simulation.agent.trigger.NormTrigger;
import nl.uu.iss.ga.simulation.agent.context.NormContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class AcceptNormPlan extends RunOncePlan<CandidateActivity> {

    private NormTrigger trigger;

    public AcceptNormPlan(NormTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) {
        NormContext context = planToAgentInterface.getContext(NormContext.class);
        context.addNorm(this.trigger.getNorm());
        return null;
    }
}
