package main.java.nl.uu.iss.ga.simulation.agent.planscheme;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.plan.ExecuteScheduledActivityPlan;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

public class GoalPlanScheme implements PlanScheme {

    @Override
    public Plan instantiate(Trigger trigger, AgentContextInterface agentContextInterface) {
        if (trigger instanceof Activity) {
            Activity activity = (Activity) trigger;

            BeliefContext context = agentContextInterface.getContext(BeliefContext.class);
            if(activity.getStart_time().getDayOfWeek().equals(context.getEnvironmentInterface().getToday())) {
                return new ExecuteScheduledActivityPlan(activity);
            }
        }

        return Plan.UNINSTANTIATED;
    }
}
