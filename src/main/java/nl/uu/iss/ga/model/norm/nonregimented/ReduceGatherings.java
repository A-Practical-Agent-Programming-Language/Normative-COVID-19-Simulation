package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;

public class ReduceGatherings extends NonRegimentedNorm {

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        Person me = agentContextInterface.getContext(Person.class);

        return (1 - beliefContext.getGovernmentTrustFactor() + 1 - me.fixedAgeRisk()) / 2;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return Arrays.asList(DetailedActivity.EXERCISE,
                DetailedActivity.RECREATIONAL_ACTIVITIES,
                DetailedActivity.RELIGIOUS_OR_OTHER_COMMUNITY_ACTIVITIES,
                DetailedActivity.VOLUNTEER_ACTIVITIES,
                DetailedActivity.VISIT_FRIENDS_OR_RELATIVES).contains(activity.getDetailed_activity());
    }
}
