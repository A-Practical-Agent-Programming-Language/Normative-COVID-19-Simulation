package nl.uu.iss.ga.simulation.agent.plan.norm;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.simulation.agent.trigger.NormTrigger;
import nl.uu.iss.ga.simulation.agent.context.NormContext;
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
        // TODO ?
//        context.replaceNorm(this.updatedNorm.getNorm());
        return null;
    }
}
