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
import main.java.nl.uu.iss.ga.util.ArgParse;
import main.java.nl.uu.iss.ga.util.GyrationRadius;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;
import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME;

public class EnvironmentInterface implements TickHookProcessor<CandidateActivity> {

    private static final int INITIATE_NORMS = 7; // TODO too hardcoded. Should be per norm
    private final AgentStateMap agentStateMap;
    private final ObservationNotifier observationNotifier;
    private final GyrationRadius gyrationRadius;

    private final Platform platform;
    private final boolean trackVisits;
    private long currentTick = 0;
    private LocalDateTime instantiated;
    private LocalDate startDate;
    private DayOfWeek today = DayOfWeek.MONDAY;
    private int node;
    private String descriptor;


    public EnvironmentInterface(Platform platform, ObservationNotifier observationNotifier, AgentStateMap agentStateMap, ArgParse argParse) {
        this.instantiated = LocalDateTime.now();
        this.platform = platform;
        this.agentStateMap = agentStateMap;
        this.observationNotifier = observationNotifier;
        this.gyrationRadius = new GyrationRadius();
        this.trackVisits = !argParse.isConnectpansim();
        this.node = argParse.getNode();
        this.startDate = argParse.getStartdate();
    }

    public long getCurrentTick() {
        return currentTick;
    }

    public DayOfWeek getToday() {
        return today;
    }

    public Random getRnd(AgentID agentID) {
        return this.agentStateMap.getRandom(agentID);
    }

    public boolean isSymptomatic(AgentID agentID) {
        return this.agentStateMap.isSymptomatic(agentID);
    }

    @Override
    public void tickPreHook(long tick) {
        this.currentTick = tick;
        this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int) (currentTick % 7 + 1));
        if (this.currentTick == INITIATE_NORMS) {
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
        Platform.getLogger().log(getClass(), Level.FINE, String.format(
                "Tick %d took %d milliseconds for %d agents (roughly %fms per agent)",
                tick, lastTickDuration, hashMap.size(), (double) lastTickDuration / hashMap.size()));

        long startCalculate = System.currentTimeMillis();
        double radius = this.gyrationRadius.calculateAverageTickRadius(tick, hashMap);
        String today = this.startDate == null ? "" : this.startDate.plusDays(currentTick).format(ISO_LOCAL_DATE) + "\t";
        Platform.getLogger().log(getClass(), Level.INFO, String.format("%s%s\t%d\t%f", today, this.today, tick, radius));
        Platform.getLogger().log(getClass(), Level.FINE, String.format(
                "Calculated radius of gyration in %d milliseconds", System.currentTimeMillis() - startCalculate
        ));
        if (this.trackVisits) {
            startCalculate = System.currentTimeMillis();
            storeLocationData(hashMap);
            Platform.getLogger().log(getClass(), Level.FINE, String.format(
                    "Stored locations in %d milliseconds", System.currentTimeMillis() - startCalculate));
        }
    }

    @Override
    public void simulationFinishedHook(long l, int i) {
        String descriptor = this.descriptor == null ? "" : this.descriptor;
        if(this.descriptor != null) {
            if(!(descriptor.startsWith("-") || descriptor.startsWith("_")))
                    descriptor = "-" + descriptor;
            if (!(descriptor.endsWith("-") | descriptor.endsWith("_")))
                descriptor += "-";
        }

        File fout = new File("output", String.format(
                "radius-of-gyration-%s%s%s.csv",
                this.node >= 0 ? "node-" + this.node : "",
                descriptor,
                this.instantiated.format(ISO_LOCAL_DATE_TIME)
        ));
        try {
            if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs()) || !(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getAbsolutePath());
            }
            this.gyrationRadius.writeResults(fout, this.agentStateMap.getAgentToPidMap(), this.startDate, true);
        } catch (IOException e) {
            Platform.getLogger().log(getClass(), Level.SEVERE, "Failed to write gyration results to file " + fout.getAbsolutePath());
            Platform.getLogger().log(getClass(), e);
        }
    }

    // TODO simulate agent visit output
    private void storeLocationData(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        HashMap<Long, TrackVisit> thisRoundVisits = getVisits(hashMap);
        notifyVisits(hashMap, thisRoundVisits);
    }

    private HashMap<Long, TrackVisit> getVisits(HashMap<AgentID, List<CandidateActivity>> agentActions) {
        HashMap<Long, TrackVisit> visits = new HashMap<>();
        for (List<CandidateActivity> actions : agentActions.values()) {
            for (CandidateActivity action : actions) {
                long locationID = action.getActivity().getLocation().getLocationID();
                RiskMitigationPolicy p = action.getRiskMitigationPolicy();
                DiseaseState state = action.getDiseaseState();

                if (!visits.containsKey(locationID)) {
                    visits.put(locationID, new TrackVisit());
                }

                visits.get(locationID).visited++;
                if (p.isMask()) visits.get(locationID).mask++;
                if (p.isDistance()) visits.get(locationID).distance++;
                if (state.equals(DiseaseState.INFECTED_SYMPTOMATIC)) visits.get(locationID).symptomatic++;
            }
        }
        return visits;
    }

    private void notifyVisits(HashMap<AgentID, List<CandidateActivity>> agentActions, HashMap<Long, TrackVisit> thisRoundVisits) {
        for (AgentID aid : agentActions.keySet()) {
            for (CandidateActivity activity : agentActions.get(aid)) {
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
