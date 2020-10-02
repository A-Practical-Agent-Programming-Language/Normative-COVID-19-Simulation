package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class EncourageTeleworkNorm extends NonRegimentedNorm {
    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        // TODO
        return 0;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getActivityType().equals(ActivityType.WORK) &&
                agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.none);
    }
}
