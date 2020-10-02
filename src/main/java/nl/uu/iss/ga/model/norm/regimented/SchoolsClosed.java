package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class SchoolsClosed extends Norm {

    private APPLIES appliesTo;

    public SchoolsClosed(String parameter) {
        appliesTo = APPLIES.valueOf(parameter);
    }

    public SchoolsClosed(APPLIES appliesTo) {
        this.appliesTo = appliesTo;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        CandidateActivity transformed = activity.clone();
        transformed.makeHome(agentContextInterface);
        return transformed;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getActivityType().equals(ActivityType.SCHOOL);
    }

    static enum APPLIES{
        K12,
        HIGHER_EDUCATION;
    }
}
