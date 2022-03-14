package nl.uu.iss.ga.simulation.agent.planscheme;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.simulation.agent.trigger.NewNormTrigger;
import nl.uu.iss.ga.simulation.agent.trigger.NormTrigger;
import nl.uu.iss.ga.simulation.agent.trigger.NormUpdatedTrigger;
import nl.uu.iss.ga.simulation.agent.plan.norm.AcceptNormPlan;
import nl.uu.iss.ga.simulation.agent.plan.norm.UpdateNormPlan;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

public class NormPlanScheme implements PlanScheme<CandidateActivity> {

    @Override
    public Plan<CandidateActivity> instantiate(Trigger trigger, AgentContextInterface agentContextInterface) {
        if(trigger instanceof NewNormTrigger) {
            return new AcceptNormPlan((NormTrigger)trigger);
        } else if (trigger instanceof NormUpdatedTrigger) {
            return new UpdateNormPlan(((NormTrigger)trigger));
        }

        return Plan.UNINSTANTIATED();
    }
}
