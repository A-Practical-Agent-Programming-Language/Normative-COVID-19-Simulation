package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.NormContainer;
import main.java.nl.uu.iss.ga.model.reader.NormScheduleReader;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.util.GyrationRadius;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import main.java.nl.uu.iss.ga.util.ScheduleTracker;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import main.java.nl.uu.iss.ga.util.tracking.InfluencedActivities;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;
import org.javatuples.Pair;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.time.format.DateTimeFormatter.ISO_LOCAL_DATE;

public class EnvironmentInterface implements TickHookProcessor<CandidateActivity> {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentInterface.class.getName());

    private final AgentStateMap agentStateMap;
    private final Map<LocalDate, List<NormContainer>> normSchedule;
    private final ObservationNotifier observationNotifier;
    private final GyrationRadius gyrationRadius;
    private final ScheduleTracker scheduleTracker;

    private final Platform platform;
    private final ArgParse arguments;
    private final boolean trackVisits;
    private long currentTick = 0;
    private final LocalDateTime instantiated;
    private final LocalDate startDate;
    private DayOfWeek today = DayOfWeek.MONDAY;

    public EnvironmentInterface(
            Platform platform,
            ObservationNotifier observationNotifier,
            AgentStateMap agentStateMap,
            NormScheduleReader normSchedule,
            ArgParse arguments
    ) {
        this.instantiated = LocalDateTime.now();
        this.platform = platform;
        this.arguments = arguments;
        this.agentStateMap = agentStateMap;
        this.normSchedule = normSchedule.getEventsMap();
        this.observationNotifier = observationNotifier;
        this.gyrationRadius = new GyrationRadius(arguments.getCounties(), this.agentStateMap);
        this.scheduleTracker = new ScheduleTracker(
                arguments.getOutputDir(),
                normSchedule.getAllUsedNorms());
        this.trackVisits = !arguments.isConnectpansim();
        this.startDate = arguments.getStartdate() == null ?
                this.normSchedule.keySet().stream().findFirst().orElse(null) : arguments.getStartdate();

        if(this.startDate != null) {
            this.today = DayOfWeek.fromDate(this.startDate);
            LOGGER.log(Level.INFO, "Start date set to " + this.startDate.format(DateTimeFormatter.ofPattern("cccc dd MMMM yyyy")));
        }
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
        if(this.startDate == null) {
            this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int) (currentTick % 7 + 1));
        } else {
            this.today = DayOfWeek.fromDate(this.startDate.plusDays(tick));
        }

        GoalPlanScheme.influencedActivitiesTracker = new InfluencedActivities(tick);

        processNormUpdates();
    }

    private void processNormUpdates() {
        LocalDate today = this.startDate.plusDays(this.currentTick);
        if(this.normSchedule.containsKey(today)) {
            for(NormContainer norm : this.normSchedule.get(today)) {
                if(norm.getStartDate().equals(today)) {
                    if(norm.getNorm() != null) {
                        this.observationNotifier.notifyNorm(norm.getNorm());
                        LOGGER.log(Level.INFO, "Activated norm " + norm.getNorm().toString());
                    }
                    if(!norm.getComment().isBlank())
                        LOGGER.log(Level.INFO, norm.getComment());
                } else if (norm.getEndDate().equals(today) && norm.getNorm() != null) {
                    this.observationNotifier.notifyNormCancelled(norm.getNorm());
                    LOGGER.log(Level.INFO, "Inactivated norm " + norm.getNorm().toString());
                }
            }
        }
    }

    @Override
    public void tickPostHook(long tick, int lastTickDuration, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        LOGGER.log(Level.FINE, String.format(
                "Tick %d took %d milliseconds for %d agents (roughly %fms per agent)",
                tick, lastTickDuration, hashMap.size(), (double) lastTickDuration / hashMap.size()));

        long startCalculate = System.currentTimeMillis();
        HashMap<String, Pair<Integer, Double>> countyAverageRadii = this.gyrationRadius.calculateAverageTickRadius(tick, hashMap);
        String today = this.startDate == null ? "" : this.startDate.plusDays(currentTick).format(ISO_LOCAL_DATE);
        for(String fips : countyAverageRadii.keySet()) {
            LOGGER.log(Level.INFO, String.format("%s %s (tick %d): [%s] %f", this.today, today, tick, fips, countyAverageRadii.get(fips).getValue1()));
        }

        File outDir = new File(this.arguments.getOutputDir());
        File fout = outDir.isAbsolute() ?
                Paths.get(this.arguments.getOutputDir(), "tick-averages.csv").toFile() :
                Paths.get("output", this.arguments.getOutputDir(), "tick-averages.csv").toFile();
        try {
            if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs())) {
                throw new IOException("Failed to create file " + fout.getAbsolutePath());
            }
            this.gyrationRadius.writeAveragesToFile(fout, countyAverageRadii, this.startDate.plusDays(tick));
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write gyration results to file " + fout.getAbsolutePath(), e);
        }

        LOGGER.log(Level.FINE, String.format(
                "Calculated radius of gyration in %d milliseconds", System.currentTimeMillis() - startCalculate
        ));
        if (this.trackVisits) {
            startCalculate = System.currentTimeMillis();
            storeLocationData(hashMap);
            LOGGER.log(Level.FINE, String.format(
                    "Stored locations in %d milliseconds", System.currentTimeMillis() - startCalculate));
        }

        this.scheduleTracker.processTick(this.startDate.plusDays(tick), hashMap);
    }

    @Override
    public void simulationFinishedHook(long l, int i) {
        for(ConfigModel county : this.arguments.getCounties()) {
            File outDir = new File(this.arguments.getOutputDir());
            File fout = outDir.isAbsolute() ?
                    Paths.get(this.arguments.getOutputDir(), county.getOutFileName()).toFile() :
                    Paths.get("output", this.arguments.getOutputDir(), county.getOutFileName()).toFile();
            try {
                if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs()) || !(fout.exists() || fout.createNewFile())) {
                    throw new IOException("Failed to create file " + fout.getAbsolutePath());
                }
                this.gyrationRadius.writeResults(county, fout, this.agentStateMap.getAgentToPidMap(), this.startDate, true);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Failed to write gyration results to file " + fout.getAbsolutePath(), e);
            }
        }
    }

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
