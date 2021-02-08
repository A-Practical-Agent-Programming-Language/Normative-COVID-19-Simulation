package main.java.nl.uu.iss.ga.simulation;

import main.java.nl.uu.iss.ga.Simulation;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.model.norm.NormContainer;
import main.java.nl.uu.iss.ga.model.reader.NormScheduleReader;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.util.ObservationNotifier;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.tracking.GyrationRadius;
import main.java.nl.uu.iss.ga.util.tracking.InfluencedActivities;
import main.java.nl.uu.iss.ga.util.tracking.ScheduleTracker;
import main.java.nl.uu.iss.ga.util.tracking.VisitGraph;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EnvironmentInterface implements TickHookProcessor<CandidateActivity> {

    private static final Logger LOGGER = Logger.getLogger(EnvironmentInterface.class.getName());

    private final AgentStateMap agentStateMap;
    private final Map<LocalDate, List<NormContainer>> normSchedule;
    private final ObservationNotifier observationNotifier;
    private final GyrationRadius gyrationRadius;
    private final ScheduleTracker scheduleTracker;
    private final Set<Class<? extends Norm>> allUsedNorms;

    private final Platform platform;
    private final ArgParse arguments;
    private final boolean trackVisits;
    private long currentTick = 0;
    private LocalDateTime simulationStarted;
    private final LocalDate startDate;
    private DayOfWeek today = DayOfWeek.MONDAY;

    public EnvironmentInterface(
            Platform platform,
            ObservationNotifier observationNotifier,
            AgentStateMap agentStateMap,
            NormScheduleReader normSchedule,
            ArgParse arguments
    ) {
        this.platform = platform;
        this.arguments = arguments;
        this.agentStateMap = agentStateMap;
        this.normSchedule = normSchedule.getEventsMap();
        this.allUsedNorms = normSchedule.getAllUsedNorms();
        this.observationNotifier = observationNotifier;
        this.scheduleTracker = new ScheduleTracker(
                arguments.getOutputDir(),
                this.agentStateMap,
                normSchedule);
        this.trackVisits = !arguments.isConnectpansim();
        this.startDate = arguments.getStartdate() == null ?
                this.normSchedule.keySet().stream().findFirst().orElse(null) : arguments.getStartdate();

        if(this.startDate != null) {
            this.today = DayOfWeek.fromDate(this.startDate);
            LOGGER.log(Level.INFO, "Start date set to " + this.startDate.format(DateTimeFormatter.ofPattern("cccc dd MMMM yyyy")));
        }

        this.gyrationRadius = new GyrationRadius(arguments.getOutputDir(), this.startDate, arguments.getCounties());
    }

    public void setSimulationStarted() {
        this.simulationStarted = LocalDateTime.now();
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
        if (this.startDate == null) {
            this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int) (currentTick % 7 + 1));
        } else {
            this.today = DayOfWeek.fromDate(this.startDate.plusDays(tick));
        }

        String date = this.startDate.plusDays(tick).format(DateTimeFormatter.ISO_DATE);

        if(tick < arguments.getDiseaseSeedDays() && arguments.isDiseaseSeeding()) {
                    agentStateMap.seed_infections(date, arguments.getSystemWideRandom(), arguments.getDiseaseSeedNumAgentsPerDay());
        } else if(arguments.getAdditionalDiseaseSeedNumber() != null && tick % arguments.getAdditionalEveryOtherDays() == 0) {
            agentStateMap.seed_infections(date, arguments.getSystemWideRandom(), arguments.getAdditionalDiseaseSeedNumber());
        }

        // Reset tracker for influenced activities
        GoalPlanScheme.influencedActivitiesTracker = new InfluencedActivities(tick, this.allUsedNorms);

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

        // Calculate and store radius of gyration
        long startCalculate = System.currentTimeMillis();
        this.gyrationRadius.processSimulationDay(tick, hashMap);
        LOGGER.log(Level.FINE, String.format(
                "Calculated and stored radius of gyration in %d milliseconds", System.currentTimeMillis() - startCalculate
        ));

        // Calculate and store visit history of each location
        if (this.trackVisits) {
            startCalculate = System.currentTimeMillis();
            storeLocationData(hashMap);
            LOGGER.log(Level.FINE, String.format(
                    "Stored locations in %d milliseconds", System.currentTimeMillis() - startCalculate));
        }

        if(this.arguments.writeGraph()){
            startCalculate = System.currentTimeMillis();
            String date = this.startDate.plusDays(tick).format(DateTimeFormatter.ISO_DATE);
            VisitGraph vg = new VisitGraph(date, hashMap, this.agentStateMap);
            vg.createVisitEdges(this.arguments.getOutputDir());
            vg.createVisitNodes(this.arguments.getOutputDir());
            LOGGER.log(Level.FINE, String.format(
                    "Calculated and stored edges for the visit graph in %d milliseconds",
                    System.currentTimeMillis() - startCalculate
            ));
        }

        // Calculate and store effects of norms on activities
        startCalculate = System.currentTimeMillis();
        this.scheduleTracker.processTick(this.startDate.plusDays(tick), hashMap);
        LOGGER.log(Level.FINE, String.format(
                "Stored schedule tracking logs in %d milliseconds", System.currentTimeMillis() - startCalculate));
    }

    @Override
    public void simulationFinishedHook(long l, int i) {
        Duration startupDuration = Duration.between(Simulation.instantiated, this.simulationStarted);
        Duration simulationDuration = Duration.between(this.simulationStarted, LocalDateTime.now());
        Duration combinedDuration = Duration.between(Simulation.instantiated, LocalDateTime.now());
        LOGGER.log(Level.INFO, String.format(
                "Simulation finished.\n\tInitialization took\t\t%s.\n\tSimulation took\t\t\t%s for %d time steps.\n\tTotal simulation time:\t%s",
                prettyPrint(startupDuration),
                prettyPrint(simulationDuration),
                l,
                prettyPrint(combinedDuration))
        );
    }

    /**
     * From @url{https://stackoverflow.com/questions/3471397/how-can-i-pretty-print-a-duration-in-java#answer-16323209}
     * @param duration  Duration object to pretty print
     * @return          Pretty printed duration
     */
    private String prettyPrint(Duration duration) {
        return duration.toString()
                .substring(2)
                .replaceAll("(\\d[HMS])(?!$)", "$1 ")
                .toLowerCase();
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
