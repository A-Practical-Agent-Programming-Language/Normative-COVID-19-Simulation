package nl.uu.iss.ga.model.factor;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public class FractionMask implements IFactor{

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        return context.getLastDayFractionMaskAt(locationid);
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof FractionMask;
    }
}
