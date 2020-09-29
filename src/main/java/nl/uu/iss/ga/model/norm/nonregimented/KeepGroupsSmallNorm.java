package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class KeepGroupsSmallNorm extends NonRegimentedNorm {

    public String PARAM_MAX_GROUP_SIZE = "maxGrouPSize";

    private static final double CURVE_SLOPE_FACTOR = .2;

    private static final int N_DAYS_LOOKBACK = 7;

    public KeepGroupsSmallNorm() {
        setParameter(PARAM_MAX_GROUP_SIZE, 10);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);

        double averageSeenPreviously = historyContext.getLastDaysSeenAtAverage(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());
        double averageSymptomaticPreviously = historyContext.getLastDaysFractionSymptomatic(N_DAYS_LOOKBACK);

        // The difference between allowed and observed (larger than 0, otherwise this norm is not applicable)
        // We calculate symptomatic people double, to increase odds of adhering
        double diff = averageSeenPreviously - (int) getParameter(PARAM_MAX_GROUP_SIZE);

        // Normalize the difference to be between 0 and 1
        double normalizedDiff = 1 / (CURVE_SLOPE_FACTOR * diff + 1);
        double gov = 1 - beliefContext.getGovernmentTrustFactor();
        double fraction_symptomatic_factor = 1 - averageSymptomaticPreviously;

        // Weigh the value by how much the agent wants to follow norm and the risk involved with the number of symptomatic people
        return (normalizedDiff + gov + fraction_symptomatic_factor) / 3;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        LocationHistoryContext context = agentContextInterface.getContext(LocationHistoryContext.class);
        double averageSeenPreviously = context.getLastDaysSeenAtAverage(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());
        return !activity.getActivityType().equals(ActivityType.HOME) && averageSeenPreviously > (int) getParameter(PARAM_MAX_GROUP_SIZE);
    }
}
