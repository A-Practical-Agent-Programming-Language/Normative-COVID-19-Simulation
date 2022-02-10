package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.List;

public abstract class NonRegimentedNorm extends Norm {

    /**
     * Calculates the agents attitude towards the norm, where the value indicates the agents tendency to ignore
     * this norm for the given activity.
     *
     * @param agentContextInterface Interface to agent's belief context
     * @param activity              Activity for which to consider the norm
     * @return                      Value between 0 and 1, with 1 indicating a higher tendency to violate this norm
     */
    public abstract double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity);

    public abstract List<IFactor> getFactors();
}
