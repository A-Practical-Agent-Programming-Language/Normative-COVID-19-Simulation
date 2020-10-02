package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class StayHomeNorm extends NonRegimentedNorm {

    private int age = -1;
    private APPLIES appliesTo;

    public StayHomeNorm(String parameter) {
        if(parameter.toLowerCase().contains("all")) {
            this.appliesTo = APPLIES.ALL;
        } else if (parameter.toLowerCase().contains("age")) {
            appliesTo = APPLIES.AGE;
            this.age = ParserUtil.parseIntInString(parameter);
        } else {
            appliesTo = APPLIES.NONE; // shouldn't happen
        }
    }

    public StayHomeNorm() {
        this.appliesTo = APPLIES.ALL;
    }

    public StayHomeNorm(int fromAge) {
        this.age = fromAge;
        this.appliesTo = fromAge >= 0 ? APPLIES.AGE : APPLIES.ALL;
    }

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
        if(this.appliesTo.equals(APPLIES.NONE))
            return false;
        if(this.appliesTo.equals(APPLIES.AGE)) {
            return !activity.getActivityType().equals(ActivityType.HOME) && agentContextInterface.getContext(Person.class).getAge() >= this.age;
        } else {
            return !activity.getActivityType().equals(ActivityType.HOME);
        }
    }

    static enum APPLIES{
        NONE,
        AGE,
        ALL;
    }
}
