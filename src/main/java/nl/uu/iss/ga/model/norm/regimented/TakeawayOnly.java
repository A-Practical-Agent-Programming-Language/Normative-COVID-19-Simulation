package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Random;

public class TakeawayOnly extends Norm {

    public static final double CANCEL_EAT_OUT_PROBABILITY = .5;
    public static final double ASSUME_RANDOM_ACTIVITY_IS_EATING_OUT = .2;

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO? also, randomly cancel?

        Random rnd = agentContextInterface.getContext(BeliefContext.class).getRandom();
        if(rnd.nextDouble() > CANCEL_EAT_OUT_PROBABILITY) {
            CandidateActivity transformed = activity.clone();
            transformed.getActivity().setDuration(20 * 60); // TODO 20 minutes?
            return transformed;
        } else {
            return null;
        }
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO this was in the detailed activity types. Should we randomly pretend an activity of type OTHER
        // is going out for dinner? What probability should we use?

        return activity.getActivityType().equals(ActivityType.OTHER) &&
                activity.getDuration() > 20 * 60 &&
                agentContextInterface.getContext(BeliefContext.class).getRandom().nextDouble() <
                        ASSUME_RANDOM_ACTIVITY_IS_EATING_OUT;
    }
}
