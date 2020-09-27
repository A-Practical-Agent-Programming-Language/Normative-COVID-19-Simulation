package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class KeepGroupsSmallNorm extends NonRegimentedNorm {

    public String PARAM_MAX_GROUP_SIZE = "maxGrouPSize";

    private static final double CURVE_SLOPE_FACTOR = .2;

    private static final int N_DAYS_LOOKBACK = 4;

    public KeepGroupsSmallNorm() {
        setParameter(PARAM_MAX_GROUP_SIZE, 10);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        double averageSeenPreviously = getAverageForActivity(beliefContext, activity, N_DAYS_LOOKBACK);

        int symptomatic = beliefContext.getEnvironmentInterface()
                .getLocationHistory(activity.getLocation().getLocationID()).getSymptomaticLastNDays(N_DAYS_LOOKBACK);

        // The difference between allowed and observed (larger than 0, otherwise this norm is not applicable)
        // We calculate symptomatic people double, to increase odds of adhering
        double diff = averageSeenPreviously - (int) getParameter(PARAM_MAX_GROUP_SIZE);

        // Normalize the difference to be between 0 and 1
        double normalizedDiff = 1 / (CURVE_SLOPE_FACTOR * diff + 1);

        double gov = 1 - beliefContext.getGovernmentTrustFactor();

        // TODO this would make more sense for just people encountered, as a general measure for how many people are ill around you
        double fraction_symptomatic_factor = 1 - (((double) symptomatic / N_DAYS_LOOKBACK) / averageSeenPreviously);

        // Weigh the value by how much the agent wants to follow norm and the risk involved with the number of symptomatic people
        return (normalizedDiff + gov + fraction_symptomatic_factor) / 3;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        double averageSeenPreviously = getAverageForActivity(beliefContext, activity, N_DAYS_LOOKBACK);
        return !activity.getActivityType().equals(ActivityType.HOME) && averageSeenPreviously > (int) getParameter(PARAM_MAX_GROUP_SIZE);
    }

    private double getAverageForActivity(BeliefContext beliefContext, Activity activity, int days) {
        int previouslySeen = beliefContext.getEnvironmentInterface()
                .getLocationHistory(activity.getLocation().getLocationID()).getVisitedLastNDays(days);

        return (previouslySeen / (double) days);
    }
}
