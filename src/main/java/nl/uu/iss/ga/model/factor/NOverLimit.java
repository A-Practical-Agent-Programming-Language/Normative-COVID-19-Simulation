package nl.uu.iss.ga.model.factor;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public class NOverLimit implements IFactor {

    private final int maxAllowed;
    private final double curve_slope_factor;

    public NOverLimit(int maxAllowed, double curve_slope_factor) {
        this.maxAllowed = maxAllowed;
        this.curve_slope_factor = curve_slope_factor;
    }

    @Override
    public double calculateValue(long locationid, LocationHistoryContext context) {
        int seen = context.getLastDaySeenAt(locationid);
        if (seen <= this.maxAllowed) {
            return 1;
        } else {
            double diff = seen - this.maxAllowed;
            return  (1 / (curve_slope_factor * diff + 1));
        }
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof NOverLimit;
    }

}
