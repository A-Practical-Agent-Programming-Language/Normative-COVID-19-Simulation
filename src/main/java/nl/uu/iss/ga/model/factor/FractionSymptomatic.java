package main.java.nl.uu.iss.ga.model.factor;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public class FractionSymptomatic implements IFactor {

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return Math.max(1, context.getLastDayFractionSymptomaticAt(locationid) + 0.5);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FractionSymptomatic;
    }
}
