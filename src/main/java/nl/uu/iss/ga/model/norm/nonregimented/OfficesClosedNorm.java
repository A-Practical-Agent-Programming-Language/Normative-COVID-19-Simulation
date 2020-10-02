package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class OfficesClosedNorm extends NonRegimentedNorm {

    public static final int N_DAYS_LOOKBACK = 10;

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getActivityType().equals(ActivityType.WORK) &&
                agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.none);
    }

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);

        // How many symptomatic people were there at the office previously
        double fractionSymptomatic =
                1 - historyContext.getLastDaysFractionSymptomaticAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

        // TODO, implements this based on job type. Necessary?
        return (fractionSymptomatic + 1 - beliefContext.getGovernmentTrustFactor()) / 2;
    }
}
