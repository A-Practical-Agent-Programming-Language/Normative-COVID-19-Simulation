package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class StayHomeNorm extends NonRegimentedNorm {

    public static final String PARAM_NAME_APPLIES_TO = "appliesTo";
    public static final String PARAM_VALUE_ELDERLY = "65+";
    public static final String PARAM_VALUE_ALL = "all";

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
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
