package nl.uu.iss.ga.model.norm.modal;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.factor.FractionDistance;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

import java.util.Arrays;
import java.util.List;

/**
 * See @code{ModalNorm} for all reasoning
 */
public class MaintainDistanceNorm extends ModalNorm {

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.clone().setNormApplied(this).setDistancing(true);
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days) {
        return locationHistoryContext.getLastDaysFractionDistancing(days);
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days) {
        return locationHistoryContext.getLastDaysFractionDistancingAt(days, locationID);
    }

    @Override
    public String toString() {
        return "maintainDistance";
    }

    @Override
    public List<IFactor> getFactors() {
        return Arrays.asList(
                new FractionDistance()
        );
    }
}
