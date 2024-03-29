package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

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
        // TODO ideally we use the severity of the symptoms, but we do not have that data
        return 1 - beliefContext.getPriorTrustAttitude();
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
}
