package main.java.nl.uu.iss.ga.simulation.agent.plan.activity;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.TripActivity;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class HandleTripPlan extends RunOncePlan<CandidateActivity> {

    private final TripActivity trip;

    public HandleTripPlan(TripActivity trip) {
        this.trip = trip;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planInterface) throws PlanExecutionError {
        DayPlanContext context = planInterface.getContext(DayPlanContext.class);
        context.setLastTripActivity(this.trip);
        return null;
    }
}
