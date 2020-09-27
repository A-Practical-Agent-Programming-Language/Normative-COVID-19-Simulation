package main.java.nl.uu.iss.ga.model.norm.regimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class TakeawayOnly extends Norm {

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        CandidateActivity transformed = activity.clone();
        transformed.getActivity().setDuration(20 * 60); // TODO 20 minutes?

        // TODO this is a problem, do we assume people just eat in their cars? Otherwise they need to go home earlier

        return transformed;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getDetailed_activity().equals(DetailedActivity.BUY_MEALS) &&
                activity.getDuration() > 20 * 60; // TODO? also, randomly cancel?
    }
}
