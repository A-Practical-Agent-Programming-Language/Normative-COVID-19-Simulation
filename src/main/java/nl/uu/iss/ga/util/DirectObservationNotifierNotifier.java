package main.java.nl.uu.iss.ga.util;

import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.NormContext;
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
    private Map<AgentID, NormContext> agentNormContextMap;

    public DirectObservationNotifierNotifier(Platform platform) {
        this.pidAgentMap = new HashMap<>();
        this.agentHistoryContextMap = new HashMap<>();
        this.agentNormContextMap = new HashMap<>();
    }

    @Override
    public void notifyNorm(AgentID agentID, Norm norm) {
        this.agentNormContextMap.get(agentID).addNorm(norm);
    }

    @Override
    public void notifyNorm(long pid, Norm norm) {
        this.agentNormContextMap.get(this.pidAgentMap.get(pid)).addNorm(norm);
    }

    public void notifyNorm(Norm norm) {
        this.agentNormContextMap.values().forEach(x -> x.addNorm(norm));
    }

    @Override
    public void notifyNormCancelled(AgentID agentID, Norm norm) {
        this.agentNormContextMap.get(agentID).removeNorm(norm);
    }

    @Override
    public void notifyNormCancelled(long pid, Norm norm) {
        this.agentNormContextMap.get(this.pidAgentMap.get(pid)).removeNorm(norm);
    }

    @Override
    public void notifyNormCancelled(Norm norm) {
        this.agentNormContextMap.values().forEach(x -> x.removeNorm(norm));
    }

    @Override
    public void notifyVisit(AgentID agentID, long tick, LocationHistoryContext.Visit visit) {
        this.agentHistoryContextMap.get(agentID).addVisit(tick, visit);
    }

    @Override
    public void notifyVisit(long pid, long tick, LocationHistoryContext.Visit visit) {
        this.agentHistoryContextMap.get(this.pidAgentMap.get(pid)).addVisit(tick, visit);
    }

    public void addNormContext(AgentID aid, long pid, NormContext normContext) {
        this.pidAgentMap.put(pid, aid);
        this.agentNormContextMap.put(aid, normContext);
    }

    public void addLocationHistoryContext(AgentID aid, long pid, LocationHistoryContext locationHistoryContext) {
        this.pidAgentMap.put(pid, aid);
        this.agentHistoryContextMap.put(aid, locationHistoryContext);
    }
}
