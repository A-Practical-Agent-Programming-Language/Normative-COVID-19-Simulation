package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.data.Activity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class ExecuteScheduledActivityPlan extends RunOncePlan {

    private Activity activity;

    public ExecuteScheduledActivityPlan(Activity activity) {
        this.activity = activity;
    }

    @Override
    public Object executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        // TODO reasoning about maintaining distance and wearing mask here?

//        System.out.printf("%s\tExecuting %s %s activity of %s: %s\n",
//                planToAgentInterface.getAgentID(),
//                activity.getStart_time().getDayOfWeek(),
//                activity.getStart_time().getArmy_time_of_day(),
//                activity.getActivityType(),
//                activity.getDetailed_activity());

        return activity.getDetailed_activity().toString();
    }
}
