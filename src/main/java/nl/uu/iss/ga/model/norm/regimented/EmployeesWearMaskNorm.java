package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

/**
 * Modeled as a regimented norm. If people work in retail, they will always wear a mask to work
 */
public class EmployeesWearMaskNorm extends Norm {
    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        activity.setMask(true);
        return activity;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getActivityType().equals(ActivityType.WORK) &&
                agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.retail);
    }

    @Override
    public String toString() {
        return "EmployeesWearMasks";
    }
}
