package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class MaintainDistanceNorm extends ModalNorm {

    public MaintainDistanceNorm() {
        this.setParameter("distance", "1.5m");
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.clone().setNormApplied(this).setDistancing(true);
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

    @Override
    double getNDaysWithMode(EnvironmentInterface.LocationHistory history, int days) {
        return history.getDistanceLastNDays(days);
    }
}
