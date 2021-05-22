package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class SleepPlan extends RunOncePlan<CandidateActivity> {

    private final SleepGoal goal;

    public SleepPlan(SleepGoal goal) {
        this.goal = goal;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        try {
            Thread.sleep(this.goal.getSleepTime());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return null;
    }
}
