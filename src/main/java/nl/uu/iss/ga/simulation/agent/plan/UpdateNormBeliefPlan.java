package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.norm.NormNotificationTrigger;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;

public class UpdateNormBeliefPlan extends Plan {

    private NormNotificationTrigger trigger;

    public UpdateNormBeliefPlan(NormNotificationTrigger trigger) {
        this.trigger = trigger;
    }

    @Override
    public Object execute(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        NormContext context = planToAgentInterface.getContext(NormContext.class);
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);

        // TODO, this just means they will comply. Should be based on willingness to comply?
        context.addNorm(this.trigger.getNorm(), beliefContext.getGovernmentTrustFactor());
        this.setFinished(true);
        return null;
    }
}
