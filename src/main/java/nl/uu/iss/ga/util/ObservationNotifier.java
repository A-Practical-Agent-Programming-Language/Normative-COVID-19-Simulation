package nl.uu.iss.ga.util;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

public interface ObservationNotifier {

    void notifyVisit(AgentID agentID, long timeStep, LocationHistoryContext.Visit visit);

    void notifyVisit(long pid, long timeStep, LocationHistoryContext.Visit visit);

    void addLocationHistoryContext(AgentID aid, long pid, LocationHistoryContext locationHistoryContext);
}
