package main.java.nl.uu.iss.ga.util;

import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

public interface ObservationNotifier {

    void notifyVisit(AgentID agentID, long tick, LocationHistoryContext.Visit visit);

    void notifyVisit(long pid, long tick, LocationHistoryContext.Visit visit);

    void addLocationHistoryContext(AgentID aid, long pid, LocationHistoryContext locationHistoryContext);
}
