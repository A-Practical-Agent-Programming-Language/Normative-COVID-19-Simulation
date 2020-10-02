package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;
import java.util.List;

public class KeepGroupsSmallNorm extends NonRegimentedNorm {
    private static final double CURVE_SLOPE_FACTOR = .2;
    private static final int N_DAYS_LOOKBACK = 7;

    private APPLIES appliesToSetting;
    private int maxAllowed;

    public KeepGroupsSmallNorm(String parameter) {
        boolean pu = parameter.toLowerCase().contains("public");
        boolean pr = parameter.toLowerCase().contains("private");
        if((pr && pu) || parameter.toLowerCase().contains("pp")) {
            appliesToSetting = APPLIES.ALL;
        } else if (pr) {
            appliesToSetting = APPLIES.PRIVATE;
        } else if (pu) {
            appliesToSetting = APPLIES.PUBLIC;
        } else {
            appliesToSetting = APPLIES.NONE;
        }
        maxAllowed = ParserUtil.parseAsInt(parameter.split(",")[0]);
    }

    public KeepGroupsSmallNorm(APPLIES appliesToSetting, int maxAllowed) {
        this.appliesToSetting = appliesToSetting;
        this.maxAllowed = maxAllowed;
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);

        double averageSeenPreviously = historyContext.getLastDaysSeenAtAverage(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());
        double averageSymptomaticPreviously = historyContext.getLastDaysFractionSymptomatic(N_DAYS_LOOKBACK);

        // The difference between allowed and observed (larger than 0, otherwise this norm is not applicable)
        // We calculate symptomatic people double, to increase odds of adhering
        double diff = averageSeenPreviously - this.maxAllowed;

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
        if(!this.appliesToSetting.applicableActivityTypes.contains(activity.getActivityType()))
            return false;

        LocationHistoryContext context = agentContextInterface.getContext(LocationHistoryContext.class);
        double averageSeenPreviously = context.getLastDaysSeenAtAverage(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());
        return averageSeenPreviously > this.maxAllowed;
    }

    public APPLIES getAppliesToSetting() {
        return appliesToSetting;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    static enum APPLIES {
        NONE(),
        PUBLIC(ActivityType.RELIGIOUS, ActivityType.UNKNOWN_OTHER, ActivityType.OTHER),
        PRIVATE(ActivityType.OTHER, ActivityType.RELIGIOUS, ActivityType.UNKNOWN_OTHER),
        ALL(ActivityType.WORK, ActivityType.SHOP, ActivityType.OTHER, ActivityType.SCHOOL, ActivityType.UNKNOWN_OTHER, ActivityType.RELIGIOUS);

        private List<ActivityType> applicableActivityTypes;

        APPLIES(ActivityType... activityTypes) {
            this.applicableActivityTypes = Arrays.asList(activityTypes);
        }
    }
}
