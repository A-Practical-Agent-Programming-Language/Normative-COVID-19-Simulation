package nl.uu.iss.ga.model.norm.regimented;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;

/**
 * This norm indicates that business may operate with reduced capacity, and can come in two forms: Either a percentage
 * of the minimum number of people allowed under normal circumstances, or a maximum absolute number of people that
 * are allowed access to the business.
 *
 * We do not have data on the minimum number of people allowed under normal circumstances, so we model this norm as
 * applying to only that percentage of people, to simulate reduced capacity.
 */
public class ReduceBusinessCapacityNorm extends Norm {

    public static int N_DAYS_LOOKBACK = 14; // two weeks

    private int capacityPercentage = -1;
    private int maxAllowed = -1;

    public ReduceBusinessCapacityNorm(String parameter) {
        if(parameter.contains("%")) {
            this.capacityPercentage = ParserUtil.parseIntInString(parameter);
        } else {
            this.maxAllowed = ParserUtil.parseIntInString(parameter);
        }
    }

    /**
     *
     * @param capacity  Is this value a capacity percentage (true) or a maximum allowed value (false)
     * @param value     Percentage of absolute number of maximum allowed visitors (depending on capacity flag)
     */
    public ReduceBusinessCapacityNorm(boolean capacity, int value) {
        this.capacityPercentage = capacity ? value : -1;
        this.maxAllowed = capacity ? -1 : value;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    /**
     * This norm applies to all activities of type OTHER.
     *
     * If businesses are allowed to operate at X% of business, we apply this norm to (100-X)% of the people, to
     * simulate only (100-X)% of the people who usually go allowed on the property.
     *
     * If an absolute maximum is set, we allow only a fraction of people to go, based on how many people are typically
     * at this location, such that on average only the maximum number of people will go to the activity.
     *
     * THis is done by applying the norm to all people who have been randomly selected to be not allowed to go
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        if(!Arrays.asList(ActivityType.SHOP, ActivityType.OTHER).contains(activity.getActivityType())) {
            // Business visits can only be of type SHOP and OTHER
            return false;
        } else if (!activity.getLocation().getDesignation().equals(Designation.none) || activity.getLocation().isResidential()) {
            // If the visited location is essential, or a home location, assume the norm does not apply
            return false;
        } else if (this.capacityPercentage >= 0) {
            // Simulate only {this.capacityPercentage} of people going to the location
            return agentContextInterface.getContext(BeliefContext.class).getRandom().nextDouble() * 100 > this.capacityPercentage;
        } else if (this.maxAllowed >= 0) {
            int actuallySeenAverage = agentContextInterface.getContext(LocationHistoryContext.class)
                    .getLastDaysSeenAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

            if(actuallySeenAverage <= this.maxAllowed) {
                return false;
            } else {
                // Simulate only maxAllowed people going to the location (on average)
                double percentageStillAllowed = this.maxAllowed / (double) actuallySeenAverage;
                return agentContextInterface.getContext(BeliefContext.class).getRandom().nextDouble() > percentageStillAllowed;
            }
        }

        return false;
    }

    @Override
    public String toString() {
        return String.format(
                "ReduceBusinessCapacity[%s]",
                this.capacityPercentage >= 0 ? String.format("capacity=%d%%", this.capacityPercentage) :
                        String.format("max=%d", this.maxAllowed)
        );
    }
}
