package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class OfficesClosed extends NonRegimentedNorm {

    public static final int N_DAYS_LOOKBACK = 10;

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO job types influence if this norm is applicable. E.g. essential workers do not have to work from home
        return activity.getActivityType().equals(ActivityType.WORK) ||
                activity.getDetailed_activity().equals(DetailedActivity.WORK) ||
                activity.getDetailed_activity().equals(DetailedActivity.WORK_RELATED_MEETING_TRIP) ||
                activity.getDetailed_activity().equals(DetailedActivity.VOLUNTEER_ACTIVITIES);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);

        // How many symptomatic people were there at the office previously
        double fractionSymptomatic =
                1 - historyContext.getLastDaysFractionSymptomaticAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

        // TODO, implements this based on job type. Necessary?
        return (fractionSymptomatic + 1 - beliefContext.getGovernmentTrustFactor()) / 2;
    }
}
