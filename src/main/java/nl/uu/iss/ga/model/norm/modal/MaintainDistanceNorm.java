package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

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
}
