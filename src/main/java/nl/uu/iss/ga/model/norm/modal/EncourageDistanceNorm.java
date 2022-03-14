package nl.uu.iss.ga.model.norm.modal;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

/**
 * This norm is currently not used
 */
@Deprecated
public class EncourageDistanceNorm extends ModalNorm {

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

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return false;
    }
}
