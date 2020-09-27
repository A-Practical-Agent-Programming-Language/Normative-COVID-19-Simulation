package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DiseaseRiskContext;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public abstract class ModalNorm extends NonRegimentedNorm {

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        DiseaseRiskContext diseaseRiskContext = agentContextInterface.getContext(DiseaseRiskContext.class);

        EnvironmentInterface.LocationHistory history = beliefContext.getEnvironmentInterface().getLocationHistory(activity.getLocation().getLocationID());

        double fractionMode = history == null ? 1 : getNDaysWithMode(history, 4) / (double) history.getVisitedLastNDays(4);

        if(diseaseRiskContext.isSymptomatic()) {
                // TODO skew a bit, but how much?
                fractionMode += Math.random() * beliefContext.getGovernmentTrustFactor();
        }

        double attitude = (beliefContext.getGovernmentTrustFactor() + fractionMode) / 2;
        assert attitude >= 0 && attitude <= 1;
        return attitude;
    }

    abstract double getNDaysWithMode(EnvironmentInterface.LocationHistory history, int days);


}
