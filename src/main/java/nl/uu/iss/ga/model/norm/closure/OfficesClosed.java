package main.java.nl.uu.iss.ga.model.norm.closure;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class OfficesClosed extends Norm {

    public static final String NAME = "CLOSURE_OFFICES";

    public OfficesClosed() {
        super(NAME, NORM_TYPE.PROHIBITION, false);
    }

    @Override
    public CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface) {
        if (
                activity.getActivity().getActivityType().equals(ActivityType.WORK) ||
                activity.getActivity().getDetailed_activity().equals(DetailedActivity.WORK) ||
                activity.getActivity().getDetailed_activity().equals(DetailedActivity.WORK_RELATED_MEETING_TRIP) ||
                activity.getActivity().getDetailed_activity().equals(DetailedActivity.VOLUNTEER_ACTIVITIES)
        ) {
            activity.getActivity().setLocation(agentContextInterface.getContext(BeliefContext.class).getHomeLocation());
            activity.getActivity().setActivityType(ActivityType.HOME);
            activity.getActivity().setDetailed_activity(DetailedActivity.WORK_FROM_HOME);
        }
        return null;
    }
}
