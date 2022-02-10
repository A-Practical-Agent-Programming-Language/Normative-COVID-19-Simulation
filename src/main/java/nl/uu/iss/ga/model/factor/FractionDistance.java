package main.java.nl.uu.iss.ga.model.factor;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public class FractionDistance implements IFactor{

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return context.getLastDayFractionDistancingAt(locationid);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FractionDistance;
    }
}
