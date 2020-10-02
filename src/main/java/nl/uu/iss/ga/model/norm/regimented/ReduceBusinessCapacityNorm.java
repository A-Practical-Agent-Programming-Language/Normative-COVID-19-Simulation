package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class ReduceBusinessCapacityNorm extends Norm {

    private int capacityPercentage = -1;
    private int maxAllowed = -1;

    public ReduceBusinessCapacityNorm(String parameter) {
        if(parameter.contains("%")) {
            this.capacityPercentage = ParserUtil.parseIntInString(parameter);
        } else {
            this.maxAllowed = ParserUtil.parseIntInString(parameter);
        }
    }

    /**
     *
     * @param capacity  Is this value a capacity percentage (true) or a maximum allowed value (false)
     * @param value
     */
    public ReduceBusinessCapacityNorm(boolean capacity, int value) {
        this.capacityPercentage = capacity ? value : -1;
        this.maxAllowed = capacity ? -1 : value;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO? Capacity is 50% of what is normally the minumum.
        // Let's say this norm transforms an activity to NULL (i.e. cancel) in 50% of the cases?
        return false;
    }
}
