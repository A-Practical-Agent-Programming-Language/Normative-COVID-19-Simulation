package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class OutDoorActivitiesOnly extends Norm {
    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    /**
     * This norm is a placeholder, but currently never applicable, as we have no data to distinguish indoor from
     * outdoor activities.
     *
     * An alternative could be to use a probability that any given activity is an outdoor activity
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return false;
    }

    @Override
    public String toString() {
        return "ActivitiesOnlyOutdoors";
    }
}
