package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.WearMaskNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHome;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.OfficesClosed;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.ReduceGatherings;
import main.java.nl.uu.iss.ga.model.norm.regimented.SchoolsClosed;
import main.java.nl.uu.iss.ga.model.norm.regimented.TakeawayOnly;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.NewNormTrigger;
import main.java.nl.uu.iss.ga.util.GyrationRadius;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.util.HashMap;
import java.util.List;

public class EnvironmentInterface implements TickHookProcessor<CandidateActivity> {

    private static final int INITIATE_NORMS = 3; // TODO too hardcoded. Should be per norm
    private final AgentStateMap agentStateMap;
    private final ObservationNotifier observationNotifier;
    private final GyrationRadius gyrationRadius;

    private final Platform platform;

    private long currentTick = 0;
    private final boolean trackVisits;
    private DayOfWeek today = DayOfWeek.MONDAY;

    public EnvironmentInterface(Platform platform, ObservationNotifier observationNotifier, AgentStateMap agentStateMap, boolean trackVisits) {
        this.platform = platform;
        this.agentStateMap = agentStateMap;
        this.observationNotifier = observationNotifier;
        this.gyrationRadius = new GyrationRadius();
        this.trackVisits = trackVisits;
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public DayOfWeek getToday() {
        return today;
    }

    @Override
    public void tickPreHook(long tick) {
        this.currentTick = tick;
        this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int)(currentTick % 7 + 1));
        if(this.currentTick == INITIATE_NORMS) {
            Norm maskNorm = new WearMaskNorm();
            Norm distanceNorm = new MaintainDistanceNorm();
            Norm schoolsClosed = new SchoolsClosed();
            Norm officesClosed = new OfficesClosed();
            Norm smallGroupsNorm = new KeepGroupsSmallNorm();
            Norm restaurantsClosed = new TakeawayOnly();
            Norm reduceGatherings = new ReduceGatherings();
            Norm ifSickStayHome = new IfSickStayHome();
            this.platform.getAgents().values().forEach(x -> {
                x.addExternalTrigger(new NewNormTrigger(maskNorm));
                x.addExternalTrigger(new NewNormTrigger(distanceNorm));
                x.addExternalTrigger(new NewNormTrigger(schoolsClosed));
                x.addExternalTrigger(new NewNormTrigger(officesClosed));
                x.addExternalTrigger(new NewNormTrigger(restaurantsClosed));
                x.addExternalTrigger(new NewNormTrigger(smallGroupsNorm));
                x.addExternalTrigger(new NewNormTrigger(ifSickStayHome));
                x.addExternalTrigger(new NewNormTrigger(reduceGatherings));
            });
        }
    }

    @Override
    public void tickPostHook(long tick, int lastTickDuration, HashMap<AgentID, List<CandidateActivity>> hashMap) {
//        System.out.printf("Tick %d took %d milliseconds for %d agents (roughly %fms per agent)%n", tick, lastTickDuration, hashMap.size(), (double)lastTickDuration / hashMap.size());
        long startCalculate = System.currentTimeMillis();
        double radius = this.gyrationRadius.calculateAverageTickRadius(tick, hashMap);
        System.out.printf("%d%f%n", tick, radius);
//        System.out.printf("Calculated radius of gyration in %d milliseconds%n", System.currentTimeMillis() - startCalculate);
        startCalculate = System.currentTimeMillis();
        if(this.trackVisits) {
            storeLocationData(hashMap);
        }
//        System.out.printf("Stored locations in %d milliseconds%n", System.currentTimeMillis() - startCalculate);
    }

    @Override
    public void simulationFinishedHook(long l, int i) { }

    // TODO simulate agent visit output
    private void storeLocationData(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        HashMap<Long, TrackVisit> thisRoundVisits = getVisits(hashMap);
        notifyVisits(hashMap, thisRoundVisits);
    }

    private HashMap<Long, TrackVisit> getVisits(HashMap<AgentID, List<CandidateActivity>> agentActions) {
        HashMap<Long, TrackVisit> visits = new HashMap<>();
        for(List<CandidateActivity> actions : agentActions.values()) {
            for(CandidateActivity action : actions) {
                long locationID = action.getActivity().getLocation().getLocationID();
                RiskMitigationPolicy p = action.getRiskMitigationPolicy();
                DiseaseState state = action.getDiseaseState();

                if(!visits.containsKey(locationID)) {
                    visits.put(locationID, new TrackVisit());
                }

                visits.get(locationID).visited++;
                if(p.isMask()) visits.get(locationID).mask++;
                if(p.isDistance()) visits.get(locationID).distance++;
                if(state.equals(DiseaseState.INFECTED_SYMPTOMATIC)) visits.get(locationID).symptomatic++;
            }
        }
        return visits;
    }

    private void notifyVisits(HashMap<AgentID, List<CandidateActivity>> agentActions, HashMap<Long, TrackVisit> thisRoundVisits) {
        for(AgentID aid : agentActions.keySet()) {
            for(CandidateActivity activity : agentActions.get(aid)) {
                TrackVisit tv = thisRoundVisits.get(activity.getActivity().getLocation().getLocationID());
                LocationHistoryContext.Visit v = new LocationHistoryContext.Visit(
                        activity.getActivity().getPid(),
                        activity.getActivity().getLocation().getLocationID(),
                        (double) tv.symptomatic / tv.visited,
                        tv.visited,
                        tv.symptomatic,
                        tv.mask,
                        tv.distance
                );
                this.observationNotifier.notifyVisit(aid, this.currentTick, v);
            }
        }
    }

    private static class TrackVisit {
        private int visited;
        private int mask;
        private int distance;
        private int symptomatic;
    }

}
