package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

/**
 * We don't have information of restaurants, and this norm starts after our simulation ends, so
 * // TODO future work
 */
@Deprecated
public class ReduceBusinessHoursNorm extends Norm {

    public ReduceBusinessHoursNorm(String parameter) {

    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return false;
    }

    @Override
    public String toString() {
        return "ReduceBusinessHours";
    }
}
