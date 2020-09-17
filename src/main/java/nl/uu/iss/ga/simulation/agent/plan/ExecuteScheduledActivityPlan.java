package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class ExecuteScheduledActivityPlan extends RunOncePlan {

    private CandidateActivity activity;

    public ExecuteScheduledActivityPlan(CandidateActivity activity) {
        this.activity = activity;
    }

    @Override
    public Object executeOnce(PlanToAgentInterface planToAgentInterface) throws PlanExecutionError {
        // TODO reasoning about maintaining distance and wearing mask here?
        return activity.toString();
    }
}
