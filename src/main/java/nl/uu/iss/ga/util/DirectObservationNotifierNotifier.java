package nl.uu.iss.ga.util;

import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;

import java.util.HashMap;
import java.util.Map;

/**
 * Only agents should allow to modify beliefs.
 * However, belief updates (which are plans) happen before reasoning in a time step,
 * so beliefs should then be updated one time step earlier, when the beliefs are not
 * available.
 */
public class DirectObservationNotifierNotifier implements ObservationNotifier {

    private Map<Long, AgentID> pidAgentMap;
    private Map<AgentID, LocationHistoryContext> agentHistoryContextMap;

    public DirectObservationNotifierNotifier(Platform platform) {
        this.pidAgentMap = new HashMap<>();
        this.agentHistoryContextMap = new HashMap<>();
    }

    @Override
    public void notifyVisit(AgentID agentID, long tick, LocationHistoryContext.Visit visit) {
        this.agentHistoryContextMap.get(agentID).addVisit(tick, visit);
    }

    @Override
    public void notifyVisit(long pid, long tick, LocationHistoryContext.Visit visit) {
        this.agentHistoryContextMap.get(this.pidAgentMap.get(pid)).addVisit(tick, visit);
    }

    public void addLocationHistoryContext(AgentID aid, long pid, LocationHistoryContext locationHistoryContext) {
        this.pidAgentMap.put(pid, aid);
        this.agentHistoryContextMap.put(aid, locationHistoryContext);
    }
}
