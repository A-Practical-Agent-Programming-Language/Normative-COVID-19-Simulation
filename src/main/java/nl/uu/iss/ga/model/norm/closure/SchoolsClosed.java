package main.java.nl.uu.iss.ga.model.norm.closure;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class SchoolsClosed extends Norm {

    public final static String NAME = "CLOSURE_SCHOOLS";

    public SchoolsClosed() {
        super(NAME, NORM_TYPE.PROHIBITION, true);
    }

    @Override
    public CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface) {
        if(activity.getActivity().getActivityType().equals(ActivityType.SCHOOL)) {
            activity.getActivity().setActivityType(ActivityType.HOME);
            activity.getActivity().setDetailed_activity(DetailedActivity.WORK_FROM_HOME);
            activity.getActivity().setLocation(agentContextInterface.getContext(BeliefContext.class).getHomeLocation());
        }
        return null;
    }
}
