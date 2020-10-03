package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;

/**
 * See @code{ModalNorm} for all reasoning
 */
public class WearMaskPublicIndoorsNorm extends ModalNorm {
    public static final String NAME = "NORM_WEAR_MASK";

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.clone().setMask(true).setNormApplied(this);
    }

    /**
     * Applies to all indoor settings, i.e. Shopping, Religious, and "other" type activities
     * @param activity              Activity to which this norm may or may not apply
     * @param agentContextInterface Interface to agent's belief context
     *
     * @return
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return Arrays.asList(ActivityType.SHOP, ActivityType.OTHER, ActivityType.RELIGIOUS)
                .contains(activity.getActivityType());
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days) {
        return locationHistoryContext.getLastDaysFractionMask(days);
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days) {
        return locationHistoryContext.getLastDaysFractionMaskAt(days, locationID);
    }
}
