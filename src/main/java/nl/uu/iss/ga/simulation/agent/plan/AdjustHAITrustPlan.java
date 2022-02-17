package main.java.nl.uu.iss.ga.simulation.agent.plan;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.factor.IFactor;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.TrackPlansContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

public class AdjustHAITrustPlan extends RunOncePlan<CandidateActivity> {

    private static final Logger LOGGER = Logger.getLogger(AdjustHAITrustPlan.class.getSimpleName());

    private final double discountFactor;

    public AdjustHAITrustPlan(double discountFactor) {
        this.discountFactor = discountFactor;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        /*
         * TODO:
         *  - Associate all locations with an activity or activity type, to which we can apply the corresponding norm
         *  - For each location, apply all factors that are relevant according to the active norms
         *  - Calculate the global attitude for the activity
         *  - Calculate the global attitude for the day (just average over all factors, multiplied by trust?)

         */

        // Acquire relevant contexts
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);
        TrackPlansContext trackPlansContext = planToAgentInterface.getContext(TrackPlansContext.class);
        LocationHistoryContext locationHistoryContext = planToAgentInterface.getContext(LocationHistoryContext.class);

        // Get values for factors
        List<Double> factorValues = getFactorValues(trackPlansContext, locationHistoryContext);

        // Update trust
        double newTrust = calculate_trust_update(beliefContext.getPriorTrustAttitude(), factorValues);
        beliefContext.setPriorTrustAttitude(newTrust);

        // Reset tracking to get ready for a new day
        trackPlansContext.reset();

        return null;
    }

    private List<Double> getFactorValues(TrackPlansContext trackPlansContext, LocationHistoryContext locationHistoryContext) {
        List<Double> factorValues = new ArrayList<>();
        Collection<TrackPlansContext.Location> visitedLocations = trackPlansContext.getLocationBasedActivities();

        for(TrackPlansContext.Location location : visitedLocations) {
            List<IFactor> factors = location.getUniqueFactors();
            for(IFactor factor : factors) {
                factorValues.add(factor.calculateValue(location.getLocationID(), locationHistoryContext));
            }
        }

        return factorValues;
    }


    private double calculate_trust_update(double current_trust, List<Double> factors) {
        if(factors.isEmpty()) {
            return current_trust;
        } else {
            double inertia = inertia(current_trust);
            return (1 - inertia) * current_trust + inertia * average(factors);
        }
    }

    private double inertia(double trust) {
        return -4 * discountFactor * Math.pow(trust - 0.5, 2) + discountFactor;
    }

    private double average(List<Double> doubles) {
        double sum = 0;
        for(Double d : doubles) {
            sum += d;
        }
        return sum / doubles.size();
    }

}

