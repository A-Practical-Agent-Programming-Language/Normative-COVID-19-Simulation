package main.java.nl.uu.iss.ga.model.norm.closure;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class TakeawayOnly extends Norm {

    public static final String NAME = "TAKEAWAY_ONLY";

    public TakeawayOnly() {
        super(NAME, NORM_TYPE.PROHIBITION, true);
    }

    @Override
    public CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface) {
        if(activity.getActivity().getDetailed_activity().equals(DetailedActivity.BUY_MEALS) &&
                activity.getActivity().getDuration() > 20 * 60) {
            activity.getActivity().setDuration(20 * 60); // TODO 20 minutes?
        }
        return null;
    }
}
