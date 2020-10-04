package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

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


    /**
     * Produces a weight for the given value, that is close to zero, if the value is close to 0.5 (which indicates
     * an agent is ambivalent).
     *
     * Note that the weights should be used to divide the sum of the factors for weighing a norm
     *
     * See @url{https://www.desmos.com/calculator/vicfmok1xi}
     *
     * @param value     A value that should be weighted
     * @return          A weight between 0 and 1 (inclusive)
     */
    protected static double weight(double value) {
        return Math.abs(Math.pow((2 * value) - 1, 3));
    }
}
