package nl.uu.iss.ga.simulation.agent.plan;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.simulation.agent.context.TrackPlansContext;
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

        // Acquire relevant contexts
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);
        TrackPlansContext trackPlansContext = planToAgentInterface.getContext(TrackPlansContext.class);
        LocationHistoryContext locationHistoryContext = planToAgentInterface.getContext(LocationHistoryContext.class);

        // Get values for factors
        List<Double> factorValues = getFactorValues(trackPlansContext, locationHistoryContext);

        // Update trust
        double newTrust = calculate_trust_update(beliefContext.getPriorTrustAttitude(), factorValues);

        if (newTrust < 0 || newTrust > 1 || Double.isNaN(newTrust)) {
            LOGGER.severe("New trust out of bounds: " + newTrust);
        }

        beliefContext.setPriorTrustAttitude(newTrust);

        // Reset tracking to get ready for a new day
        trackPlansContext.reset();

        return null;
    }

    public static List<Double> getFactorValues(TrackPlansContext trackPlansContext, LocationHistoryContext locationHistoryContext) {
        List<Double> factorValues = new ArrayList<>();
        Collection<TrackPlansContext.Location> visitedLocations = trackPlansContext.getLocationBasedActivities();

        for(TrackPlansContext.Location location : visitedLocations) {
            List<IFactor> factors = location.getUniqueFactors();
            for(IFactor factor : factors) {
                double value = factor.calculateValue(location.getLocationID(), locationHistoryContext);
                if (value < 0 || value > 1 || Double.isNaN(value)) {
                    LOGGER.severe(String.format("Value for factor %s out of bounds: %f", factor.getClass().getSimpleName(), value));
                }
                factorValues.add(value);
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

    public static double average(List<Double> doubles) {
        double sum = 0;
        for(Double d : doubles) {
            sum += d;
        }
        return sum / doubles.size();
    }

}

