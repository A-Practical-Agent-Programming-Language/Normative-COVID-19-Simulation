package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public abstract class ModalNorm extends NonRegimentedNorm {

    protected static final int DAYS_LOOKBACK = 14;

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext locationHistoryContext = agentContextInterface.getContext(LocationHistoryContext.class);

        // TODO only look at current location?
        double fractionMode = getFractionWithModeLastDays(locationHistoryContext, DAYS_LOOKBACK);

        if(beliefContext.isSymptomatic()) {
                // TODO skew a bit, but how much?
                fractionMode += Math.random() * beliefContext.getGovernmentTrustFactor();
        }

        double attitude = (beliefContext.getGovernmentTrustFactor() + fractionMode) / 2;
        assert attitude >= 0 && attitude <= 1;
        return attitude;
    }

    abstract double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days);

    abstract double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days);

}
