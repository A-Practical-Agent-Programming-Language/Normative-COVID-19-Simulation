package nl.uu.iss.ga.model.norm.nonregimented;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;
import java.util.List;

/**
 * This norm was updated various times through the course of the first half of 2020, differing on activities
 * it applied to, and the maximum number of people allowed.
 *
 *
 * We chose to model this norm as non-regimented, and have peoples attitude depend on:
 * <ul>
 *     <li>Government trust factor (lower value means higher chance of violating)</li>
 *     <li>Number of people seen previously (only a few more than allowed means higher chance of violating)</li>
 *     <li>Fraction of those people who were symptomatic (higher value means lower chance of violating)</li>
 * </ul>
 */
public class KeepGroupsSmallNorm extends NonRegimentedNorm {
    private static final double CURVE_SLOPE_FACTOR = .4;
    private static final int N_DAYS_LOOKBACK = 14; // used to be 7;

    private final APPLIES appliesToSetting;
    private final int maxAllowed;

    private static final List<ActivityType> applicablePrivateActivityTypes =
            Arrays.asList(ActivityType.OTHER, ActivityType.RELIGIOUS, ActivityType.SCHOOL, ActivityType.COLLEGE);
    private static final List<ActivityType> applicablePublicActivityTypes =
            Arrays.asList(ActivityType.WORK, ActivityType.SHOP, ActivityType.COLLEGE);

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

        // Factors

        // Normalize the difference to be between 0 and 1, with smaller differences more likely to be ignored
        double normalizedDiff = 1 - (1 / (CURVE_SLOPE_FACTOR * diff + 1)); //https://www.desmos.com/calculator/rcfitmh0ms

        return Norm.norm_violation_posterior(beliefContext.getPriorTrustAttitude(), normalizedDiff, averageSymptomaticPreviously);
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    /**
     * THis norm applies to all outdoor activities at locations were the agent previously observed more than the
     * current maximum of people allowed.
     *
     * The consequence is that if an agent has not visited a location for the number of days they look back
     * (@code{N_DAYS_LOOKBACK}) they will observe 0 people there, and thus this norm will not apply.
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext context = agentContextInterface.getContext(LocationHistoryContext.class);

        if(ActivityType.WORK.equals(activity.getActivityType()) &&
                !agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.none)) {
            // TODO Essential workers do not have to follow this norm?
            return false;
        }

        boolean privateApplicable = this.appliesToSetting.appliesToPrivate && applicablePrivateActivityTypes.contains(activity.getActivityType());
        boolean publicApplicable = this.appliesToSetting.appliesToPublic && applicablePublicActivityTypes.contains(activity.getActivityType());

        if(!privateApplicable && !publicApplicable)
            // The current restriction does not apply to this activity type
            return false;

        if(
                !this.appliesToSetting.appliesToPrivate &&
                activity.getLocation().isResidential() &&
                Designation.none.equals(activity.getLocation().getDesignation())
        ) {
            // This is a private visit at a residential location, but the restriction does not (yet) apply to
            // private settings
            return false;
        }

        // Norm applies to activity type and setting. Check if agent would violate group size
        double averageSeenPreviously = context.getLastDaysSeenAtAverage(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

        // A fraction of the people can randomly still go as a proxy for communication of who will show up
        // The norm does not apply to those randomly selected agents.
        // The following is always true if maxAllowed > averageSeenPreviously
        return beliefContext.getRandom().nextDouble() > (this.maxAllowed / averageSeenPreviously);
    }

    public APPLIES getAppliesToSetting() {
        return appliesToSetting;
    }

    public int getMaxAllowed() {
        return maxAllowed;
    }

    public enum APPLIES {
        NONE(false, false),
        PUBLIC(false, true),
        PRIVATE(true, false),
        ALL(true, true);


        private final boolean appliesToPrivate;
        private final boolean appliesToPublic;

        APPLIES(boolean appliesToPrivate, boolean appliesToPublic) {
            this.appliesToPrivate = appliesToPrivate;
            this.appliesToPublic = appliesToPublic;
        }
    }

    @Override
    public String toString() {
        return String.format("KeepGroupsSmall[appliesTo=%s, max=%d]", appliesToSetting, maxAllowed);
    }
}
