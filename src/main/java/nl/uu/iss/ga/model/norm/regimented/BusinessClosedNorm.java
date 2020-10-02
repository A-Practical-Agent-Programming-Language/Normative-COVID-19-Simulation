package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class BusinessClosedNorm extends Norm {

    private APPLIES appliesTo;

    public BusinessClosedNorm(String parameter) {
        if(parameter.contains("NEB"))
            this.appliesTo = APPLIES.NONESSENTIALBUSINESSES;
        else if(parameter.contains("DMV"))
            this.appliesTo = APPLIES.DMV;
        else
            this.appliesTo = APPLIES.NONE;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO? We don't really have "business" as data
        return false;
    }

    public APPLIES getAppliesTo() {
        return appliesTo;
    }

    static enum APPLIES {
        NONE,
        NONESSENTIALBUSINESSES,
        DMV;
    }
}
