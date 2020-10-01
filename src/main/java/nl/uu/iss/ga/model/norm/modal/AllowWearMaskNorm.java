package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class AllowWearMaskNorm extends ModalNorm{
    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days) {
        return 0;
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days) {
        return 0;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }
}
