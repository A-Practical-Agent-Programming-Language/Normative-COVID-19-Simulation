package nl.uu.iss.ga.model.factor;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.iss.ga.util.config.SimulationArguments;

public class FractionSymptomatic implements IFactor {

    public static final float ALPHA = 0.2f;

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return 1 - calculateAttitude(locationid, context, 1);
    }

    /**
     * Returns true if the linear approach of calculating the attitude change based on number of symptomatic agents
     * observed. Returns false if also the fraction of symptomatic agents should be included
     *
     * @return boolean
     */
    public static boolean getUseLinearApproach() {
        return SimulationArguments.getInstance().getLinearSymptomaticFactor();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FractionSymptomatic;
    }

    public static double calculateAttitude(long locationid, LocationHistoryContext context, int days_lookback) {
        if (getUseLinearApproach()) {
            return calculateAttitudeLinear(locationid, context, days_lookback);
        } else {
            return calculateAttitudeComplex(locationid, context, days_lookback);
        }
    }

    /**
     * Calculates the trust factor of an agent based on number of symptomatic agents encountered in a time period
     * at a specific location.
     *
     * This method returns *negative evidence*, i.e., the probability to violate a norm.
     * Keep in mind that the norm_violation_posterior requires *positive* evidence.
     *
     * @param locationid
     * @param context
     * @return
     */
    private static double calculateAttitudeLinear(long locationid, LocationHistoryContext context, int days_lookback) {
        int observedSymptomatic = context.getLastDaysSeenSymptomaticAt(days_lookback, locationid);
        return Math.min(FractionSymptomatic.ALPHA * observedSymptomatic, 1);
    }
    /**
     * Calculates the trust factor of an agent based on number and fraction of symptomatic agents encountered in
     * a time period at a specific location. The larger the fraction is, the more important this fraction becomes,
     * compared to the raw number of symptomatic agents seen (regardless of the total number of agents encountered)
     *
     * This method returns *negative evidence*, i.e., the probability to violate a norm.
     * Keep in mind that the norm_violation_posterior requires *positive* evidence.
     */
    private static double calculateAttitudeComplex(long locationid, LocationHistoryContext context, int days_lookback) {
        double a = calculateAttitudeLinear(locationid, context, days_lookback);
        double b = context.getLastDaysFractionSymptomaticAt(days_lookback, locationid);

        return (1-b) * a + b * b;
    }
}
