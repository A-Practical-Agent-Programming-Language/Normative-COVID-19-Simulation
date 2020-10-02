package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
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
        return agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.none) &&
                activity.getActivityType().equals(ActivityType.WORK) &&
                this.appliesTo.equals(APPLIES.NONESSENTIALBUSINESSES);

        // TODO we ignore DMV, because we dont have this data
    }

    public APPLIES getAppliesTo() {
        return appliesTo;
    }

    enum APPLIES {
        NONE,
        NONESSENTIALBUSINESSES,
        DMV;
    }
}
