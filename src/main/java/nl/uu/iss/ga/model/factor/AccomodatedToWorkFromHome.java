package main.java.nl.uu.iss.ga.model.factor;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public class AccomodatedToWorkFromHome implements IFactor {

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return 0.45;
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof AccomodatedToWorkFromHome;
    }

}