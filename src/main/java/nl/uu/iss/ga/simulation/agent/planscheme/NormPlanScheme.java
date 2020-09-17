package main.java.nl.uu.iss.ga.simulation.agent.planscheme;

import main.java.nl.uu.iss.ga.model.norm.NormNotificationTrigger;
import main.java.nl.uu.iss.ga.simulation.agent.plan.UpdateNormBeliefPlan;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

public class NormPlanScheme implements PlanScheme {

    @Override
    public Plan instantiate(Trigger trigger, AgentContextInterface agentContextInterface) {
        if(trigger instanceof NormNotificationTrigger) {
            return new UpdateNormBeliefPlan((NormNotificationTrigger)trigger);
        }

        return Plan.UNINSTANTIATED;
    }
}
