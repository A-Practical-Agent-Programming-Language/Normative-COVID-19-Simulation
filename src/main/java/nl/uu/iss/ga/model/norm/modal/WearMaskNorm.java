package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class WearMaskNorm extends ModalNorm {

    public static final String NAME = "NORM_WEAR_MASK";

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.clone().setMask(true).setNormApplied(this);
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
