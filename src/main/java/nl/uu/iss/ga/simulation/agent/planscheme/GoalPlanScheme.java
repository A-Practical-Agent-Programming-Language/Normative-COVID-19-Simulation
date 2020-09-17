package main.java.nl.uu.iss.ga.simulation.agent.planscheme;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
import main.java.nl.uu.iss.ga.simulation.agent.plan.ExecuteScheduledActivityPlan;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;
import nl.uu.cs.iss.ga.sim2apl.core.plan.Plan;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanScheme;

import java.util.List;

public class GoalPlanScheme implements PlanScheme {

    AgentContextInterface agentContextInterface;

    @Override
    public Plan instantiate(Trigger trigger, AgentContextInterface agentContextInterface) {
        this.agentContextInterface = agentContextInterface;
        if (trigger instanceof Activity) {
            Activity activity = (Activity) trigger;
            CandidateActivity firstCandidate = new CandidateActivity(activity);
            BeliefContext context = agentContextInterface.getContext(BeliefContext.class);

            if(context.getEnvironmentInterface().getToday().equals(activity.getStart_time().getDayOfWeek())) {
                // Trigger applies to today

                if(activity.getActivityType().equals(ActivityType.HOME)) {
                    // Don't wear mask or maintain distance
                    return new ExecuteScheduledActivityPlan(firstCandidate);
                } else {
                    // Collect candidates
                    List<CandidateActivity> candidateActivities =
                            getActivityCandidates(agentContextInterface, List.of(firstCandidate));
                    CandidateActivity toPerform = selectFromCandidates(candidateActivities);

                    // Select best candidate
                    if (toPerform.getActivity() == null) {
                        return handleActivityCancelled(activity);
                    } else {
                        return new ExecuteScheduledActivityPlan(toPerform);
                    }
                }
            }
        }

        return Plan.UNINSTANTIATED;
    }

    private List<CandidateActivity> getActivityCandidates(AgentContextInterface contextInterface, List<CandidateActivity> currentCandidates) {
        NormContext normContext = contextInterface.getContext(NormContext.class);
        for(Norm norm : normContext.getActiveNorms()) {
            applyNormToCandidates(currentCandidates, norm);
        }
        return currentCandidates;
    }

    private List<CandidateActivity> applyNormToCandidates(List<CandidateActivity> candidateActivities, Norm norm) {
        for(CandidateActivity activity : candidateActivities) {
            CandidateActivity newCandidate = norm.applyTo(activity, this.agentContextInterface);
            if(newCandidate != null) {
                candidateActivities.add(newCandidate);
            }
        }
        return candidateActivities;
    }

    private CandidateActivity selectFromCandidates(List<CandidateActivity> candidateActivities) {
        // TODO
        return candidateActivities.get(0);
    }

    private Plan handleActivityCancelled(Activity activity) {
        // TODO
        return null;
    }
}
