package main.java.nl.uu.iss.ga.simulation.agent.planscheme;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.TripActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import main.java.nl.uu.iss.ga.simulation.agent.plan.activity.ExecuteScheduledActivityPlan;
import main.java.nl.uu.iss.ga.simulation.agent.plan.activity.CancelActivityPlan;
import main.java.nl.uu.iss.ga.simulation.agent.plan.activity.HandleTripPlan;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

import java.util.ArrayList;
import java.util.List;

public class GoalPlanScheme implements PlanScheme<CandidateActivity> {

    AgentContextInterface<CandidateActivity> agentContextInterface;

    @Override
    public Plan<CandidateActivity> instantiate(Trigger trigger, AgentContextInterface<CandidateActivity> agentContextInterface) {
        this.agentContextInterface = agentContextInterface;

        if (trigger instanceof Activity) {
            Activity activity = (Activity) trigger;
            BeliefContext context = agentContextInterface.getContext(BeliefContext.class);

            if(context.getEnvironmentInterface().getToday().equals(activity.getStart_time().getDayOfWeek())) {
                // Trigger applies to today

                if (activity.getActivityType().equals(ActivityType.TRIP)) {
                    try {
                        return new HandleTripPlan((TripActivity) activity);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return null;
                    }
                } else if(activity.getActivityType().equals(ActivityType.HOME)) {
                    // Don't wear mask or maintain distance
                    return new ExecuteScheduledActivityPlan(new CandidateActivity(activity));
                } else {
                    // Collect candidates
                    List<Norm> norms = getApplicableNorms(activity);

                    CandidateActivity candidate = applyNorms(activity, norms);

                    // Select best candidate
                    if (candidate == null || candidate.getActivity() == null) {
                        return new CancelActivityPlan(activity);
                    } else {
                        return new ExecuteScheduledActivityPlan(candidate);
                    }
                }

            }
        }

        return Plan.UNINSTANTIATED();
    }

    private List<Norm> getApplicableNorms(Activity activity) {
        ArrayList<Norm> norms = new ArrayList<>();
        for(Norm norm : this.agentContextInterface.getContext(NormContext.class).getNorms()) {
            if(norm.applicable(activity, this.agentContextInterface)) {
                norms.add(norm);
            }
        }
        return norms;
    }

    /**
     * Test if the agent will ignore the norm for this activity
     * @param norm          Norm to evaluate
     * @param activity      Activity to evaluate on
     * @return              True if agent ignores norm
     */
    private boolean evaluateToIgnore(Norm norm, Activity activity) {
        return norm instanceof NonRegimentedNorm &&
                Math.random() < ((NonRegimentedNorm) norm).calculateAttitude(this.agentContextInterface, activity);
    }

    private CandidateActivity applyNorms(Activity activity, List<Norm> norms) {
        CandidateActivity candidateActivity = new CandidateActivity(activity);

        for(Norm norm : norms) {
            if(evaluateToIgnore(norm, activity)) {
                candidateActivity = norm.transformActivity(candidateActivity, this.agentContextInterface);
                if(candidateActivity == null) return null;
            }
        }

        return candidateActivity;
    }
}
