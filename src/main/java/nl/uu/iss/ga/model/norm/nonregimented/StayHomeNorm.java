package nl.uu.iss.ga.model.norm.nonregimented;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;

import java.util.Collections;
import java.util.List;

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
        return 1 - agentContextInterface.getContext(BeliefContext.class).getPriorTrustAttitude();
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        if(this.appliesTo.equals(APPLIES.NONE))
            return false;

        boolean isEssential = ActivityType.WORK.equals(activity.getActivityType()) &&
                !Designation.none.equals(agentContextInterface.getContext(Person.class).getDesignation());

        // APPLIES.NONE is already excluded
        boolean appliesToAgeGroup = APPLIES.ALL.equals(this.appliesTo) ||
                agentContextInterface.getContext(Person.class).getAge() >= this.age;

        return appliesToAgeGroup && !isEssential && !activity.getActivityType().equals(ActivityType.HOME);
    }

    enum APPLIES{
        NONE,
        AGE,
        ALL;
    }

    @Override
    public String toString() {
        return String.format("StayHome[%s]",
                this.appliesTo.equals(APPLIES.AGE) ? String.format("age >= %d", this.age) :
                        this.appliesTo);
    }

    @Override
    public List<IFactor> getFactors() {
        // TODO, in this case, it may be relevant if we see other people ignorign this directive, which only happens if we're not home
        return Collections.emptyList();
    }
}
