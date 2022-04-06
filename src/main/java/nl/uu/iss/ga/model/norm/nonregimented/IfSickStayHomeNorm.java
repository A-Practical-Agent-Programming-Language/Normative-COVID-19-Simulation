package nl.uu.iss.ga.model.norm.nonregimented;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

import java.util.Collections;
import java.util.List;

/**
 * People stay home if they are sick, with a probability indicates by how much they are inclined to follow government
 * instructions, i.e. the governmentTrustFactor.
 *
 * Applies to all activities that take place outside the home environment
 */
public class IfSickStayHomeNorm extends NonRegimentedNorm {


    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext locationHistoryContext = agentContextInterface.getContext(LocationHistoryContext.class);
        double trust = beliefContext.getPriorTrustAttitude();
        long lid = activity.getLocation().getLocationID();

        // Note that negative evidence is returned, not positive evidence as required in the violation posterior
        double factor = FractionSymptomatic.calculateAttitude(lid, locationHistoryContext, N_DAYS_LOOKBACK);
        return NonRegimentedNorm.norm_violation_posterior(trust, 1 - factor);
    }



    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        BeliefContext context = agentContextInterface.getContext(BeliefContext.class);
        return !activity.getActivityType().equals(ActivityType.HOME) && context.isSymptomatic();
    }

    @Override
    public String toString() {
        return "StayHomeIfSick";
    }

    @Override
    public List<IFactor> getFactors() {
        return Collections.emptyList();
    }
}
