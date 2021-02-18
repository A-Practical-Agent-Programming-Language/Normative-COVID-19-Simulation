package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
@Deprecated
public class BeachesClosedNorm extends Norm {

    public static final String PARAM_NAME_MAX_CAPACITY = "capacity";

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    /**
     * This norm is a placeholder, that is never applicable, because we have no data on what locations or activities
     * are related to beaches.
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO how to distinguish beaches?
        return false;
    }

    @Override
    public String toString() {
        return "BeachesClosed";
    }
}
