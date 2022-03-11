package main.java.nl.uu.iss.ga.model.factor;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;

public class FractionSymptomatic implements IFactor {

    private static final float ALPHA = 0.2f;

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return Math.max(1, context.getLastDayFractionSymptomaticAt(locationid) + 0.5);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FractionSymptomatic;
    }

    private double calculateAttitudeLinear(long locationid, LocationHistoryContext context) {
        int observedSymptomatic = context.getLastDaySeenSymptomaticAt(locationid);

        // Returns likelihood to violate



        return 0d;
    }

    private double calculateAttitudeComplex(long locationid, LocationHistoryContext context) {
        double a = calculateAttitudeLinear();
        double b = context.getLastDayFractionSymptomaticAt(locationid);

        return
    }
}
