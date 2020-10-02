package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.HashMap;
import java.util.Map;

public abstract class Norm {
    /**
     * Transforms the passed activity in another activity by applying the norm
     *  @param activity              Activity to transform
     * @param agentContextInterface Interface to agent's belief context
     * @return
     */
    public abstract CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface);

    /**
     * Test if this norm is applicable to the current activity, which means that 1) the norm was intended for this
     * activity type and 2) executing the planned activity as is would violate the norm.
     *
     * @param activity              Activity to which this norm may or may not apply
     * @param agentContextInterface Interface to agent's belief context
     *
     * @return Boolean indicating if this norm applies
     */
    public abstract boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface);
}
