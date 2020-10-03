package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

/**
 * This is a class for the common reasoning that applies to both mask wearing and maintaining distance,
 * together referred to here as <i>modal</i> norms. These are itself a class of <i>non-regimented</i> norms.
 */
public abstract class ModalNorm extends NonRegimentedNorm {

    /**
     * All agents will look at the average encountered fraction of people following the behavior (i.e. wearing a
     * mask or maintaining distance) in *all* events (not just for the current location) of the last 14 days.
     */
    protected static final int DAYS_LOOKBACK = 14;


    /**
     * The attitude of the agent is determined by three factors:
     *  <ul>
     *      <li>Their general attitude towards the government</li>
     *      <li>The fraction of people previously observed following the behavior (note this goes for the past
     *      <i>n</i> for <i>all</i> events, not just for the location the agent is currently considering</li>
     *      <i>If they are symptomatic</i>
     *  </ul>
     *
     *  If the agent is symptomatic is currently just a boolean value. We use an arbitrary skew towards more likely to
     *  wear a mask if an agent is symptomatic
     */
    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        BeliefContext beliefContext = agentContextInterface.getContext(BeliefContext.class);
        LocationHistoryContext locationHistoryContext = agentContextInterface.getContext(LocationHistoryContext.class);

        // TODO only look at current location?
        double fractionMode = getFractionWithModeLastDays(locationHistoryContext, DAYS_LOOKBACK);

        if(beliefContext.isSymptomatic()) {
            // This skew is arbitrary. Multiplying to values less than 1 will result in an even lower value
            // than the smallest of the two multiplied values.
            // By multiplying a random value with the government trust factor, we assure this skew is very small
            // if the government trust factor is low, but do not guarantee this value to be small when the government
            // trust factor is high.
            // Note that the fractionMode value can grow to more than 1, which in this case indicates the agent is so
            // much in favour of wearing a mask that they will always wear it.
            fractionMode += beliefContext.getRandom().nextDouble() * beliefContext.getGovernmentTrustFactor();
        }

        return (1 - beliefContext.getGovernmentTrustFactor() + 1 - fractionMode) / 2;
    }

    abstract double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days);

    abstract double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days);

}
