package main.java.nl.uu.iss.ga.simulation.agent.plan.norm;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.NormTrigger;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class UpdateNormPlan extends RunOncePlan<CandidateActivity> {

    private NormTrigger updatedNorm;

    public UpdateNormPlan(NormTrigger updatedNorm) {
        this.updatedNorm = updatedNorm;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        NormContext context = planToAgentInterface.getContext(NormContext.class);
        context.replaceNorm(this.updatedNorm.getNorm());
        return null;
    }
}
