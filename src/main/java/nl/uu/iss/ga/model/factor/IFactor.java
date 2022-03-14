package nl.uu.iss.ga.model.factor;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

public interface IFactor {

    double calculateValue(long locationid, LocationHistoryContext context);
}
