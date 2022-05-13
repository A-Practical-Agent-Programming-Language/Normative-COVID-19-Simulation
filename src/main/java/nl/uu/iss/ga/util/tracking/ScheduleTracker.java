package nl.uu.iss.ga.util.tracking;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.disease.AgentGroup;
import nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.model.norm.NormContainer;
import nl.uu.iss.ga.model.reader.NormScheduleReader;
import nl.uu.iss.ga.pansim.state.AgentState;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import nl.uu.iss.ga.util.Constants;
import nl.uu.iss.ga.util.config.ArgParse;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.step.StepExecutor;
import org.javatuples.Pair;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Class to keep track of changes in schedule due to applied norms
 */
public class ScheduleTracker {

    private static final Logger LOGGER = Logger.getLogger(ScheduleTracker.class.getName());

    private final ArgParse arguments;
    private final StepExecutor<CandidateActivity> executor;

    public static final String AVERAGE_SCHEDULE_FILENAME = "average-schedules_%s";
    public static final String EPICURVE_FILENAME = "epicurve.sim2apl";
    public static final String ALL_HOME_PCT = "ALL_HOME_PCT";
    public static final String VISITORS_TOTAL = "VISITORS_TOTAL";
    public static final String VISIT_DURATION_TOTAL = "VISIT_DURATION_TOTAL";
    public static final String MASK_TOTAL = "MASK_TOTAL";
    public static final String DISTANCE_TOTAL = "DISTANCE_TOTAL";
    public static final String SYMPTOMATIC_TOTAL = "SYMPTOMATIC_TOTAL";
    public static final String MASK_PCT = "MASK_PCT";
    public static final String DISTANCE_PCT = "DISTANCE_PCT";
    public static final String SYMPTOMATIC_PCT = "SYMPTOMATIC_PCT";
    public static final String NORMS_ACTIVATED_HEADER = "NORMS_ACTIVATED";
    public static final String NORMS_DEACTIVATED_HEADER = "NORMS_DEACTIVATED";
    private final Map<String, ScheduleTrackerGroup> fileObjects = new HashMap<>();
    private final AgentStateMap agentStateMap;
    private final NormScheduleReader normScheduleReader;
    private final Set<Class<? extends Norm>> allUsedNorms;

    private final String suffix;

    public ScheduleTracker(StepExecutor<CandidateActivity> executor, ArgParse arguments, AgentStateMap agentStateMap, NormScheduleReader normScheduleReader) {
        this.executor = executor;
        this.arguments = arguments;
        this.suffix = arguments.getOutputDir().getName();
        this.agentStateMap = agentStateMap;
        this.normScheduleReader = normScheduleReader;
        this.allUsedNorms = normScheduleReader.getAllUsedNorms();
        createFileObjects(allUsedNorms);
    }

    private String normToAppliedActivitiesFile(Class<? extends Norm> norm) {
        return String.format("per_activity_breakout_%s_%s", norm.getSimpleName(), this.suffix);
    }

    private String activityTypeCancelledByNormFile(ActivityType type) {
        return String.format("cancelled_per_norm_breakout_%s_%s", type.name(), this.suffix);
    }

    private void createFileObjects(Set<Class<? extends Norm>> allUsedNorms) {
        List<String> activityTypeNames = Arrays.stream(ActivityType.values())
                .filter(x -> !ActivityType.TRIP.equals(x))
                .map(ActivityType::name).collect(Collectors.toList());

        List<String> averageScheduleHeaders = new ArrayList<>(List.of(
                NORMS_ACTIVATED_HEADER, NORMS_DEACTIVATED_HEADER,
                VISITORS_TOTAL, VISIT_DURATION_TOTAL, MASK_TOTAL, DISTANCE_TOTAL, SYMPTOMATIC_TOTAL, MASK_PCT, DISTANCE_PCT, SYMPTOMATIC_PCT, ALL_HOME_PCT));
        averageScheduleHeaders.addAll(activityTypeNames.stream().map(x -> x + "_COUNT").collect(Collectors.toList()));
        averageScheduleHeaders.addAll(activityTypeNames.stream().map(x -> x + "_DURATION").collect(Collectors.toList()));
        ScheduleTrackerGroup g =
                new ScheduleTrackerGroup(arguments.getOutputDir(), String.format(AVERAGE_SCHEDULE_FILENAME, this.suffix) + ".csv", averageScheduleHeaders);
        this.fileObjects.put(AVERAGE_SCHEDULE_FILENAME, g);

        List<String> epicurveHeaders = new ArrayList<>();
        for(DiseaseState state : DiseaseState.values()) {
            epicurveHeaders.add(state.name());
            for(AgentGroup group : AgentGroup.values()) {
                epicurveHeaders.add(String.format("%s_%s", group, state.name()));
            }
        }

        ScheduleTrackerGroup epicurve = new ScheduleTrackerGroup(arguments.getOutputDir(), EPICURVE_FILENAME + ".csv", epicurveHeaders);
        this.fileObjects.put(EPICURVE_FILENAME, epicurve);

        for (Class<? extends Norm> norm : allUsedNorms) {
            this.fileObjects.put(
                    normToAppliedActivitiesFile(norm),
                    new ScheduleTrackerGroup(arguments.getOutputDir(),
                            normToAppliedActivitiesFile(norm) + ".csv",
                            activityTypeNames,
                            NORMS_ACTIVATED_HEADER,
                            NORMS_DEACTIVATED_HEADER,
                            "total_cancelled",
                            "fraction_cancelled"));
        }

        List<String> activityTypeHeaders = allUsedNorms.stream().map(Class::getSimpleName).collect(Collectors.toList());
        for (ActivityType activityType : ActivityType.values()) {
            this.fileObjects.put(
                    activityTypeCancelledByNormFile(activityType),
                    new ScheduleTrackerGroup(arguments.getOutputDir(),
                            activityTypeCancelledByNormFile(activityType) + ".csv",
                            activityTypeHeaders,
                            NORMS_ACTIVATED_HEADER,
                            NORMS_DEACTIVATED_HEADER,
                            "total_visited", "total_symptomatic", "total_mask", "total_distance", "total_cancelled", "fraction_cancelled")
            );
        }
    }

    /**
     * Write all the changes to the activity schedules produced by the agent in the last timestep to the output
     * file.
     *
     * @param simulationDay Date of the current simulation day
     * @param agentActions  Actions produced by the agents during this simulation day
     */
    public void processTimeStep(LocalDate simulationDay, List<Future<DeliberationResult<CandidateActivity>>> agentActions) {
        List<String> changedNorms = findDayNorms(simulationDay);

        // Write to files what percentage of each type of activity was cancelled
        Pair<Map<String,String>, ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>>> processed = calculateActivityFractions(agentActions);
        Map<String,String> averageScheduleMap = processed.getValue0();
        averageScheduleMap.put(NORMS_ACTIVATED_HEADER, changedNorms.get(0));
        averageScheduleMap.put(NORMS_DEACTIVATED_HEADER, changedNorms.get(1));
        this.fileObjects.get(AVERAGE_SCHEDULE_FILENAME).writeKeyMapToFile(simulationDay, averageScheduleMap);
        this.fileObjects.get(EPICURVE_FILENAME).writeKeyMapToFile(simulationDay, createEpicurveMap());
        Map<Class<? extends Norm>, Map<ActivityType, Integer>> normCancelled =
                GoalPlanScheme.influencedActivitiesTracker.getActivitiesCancelledByNorm();

        // Write to files what activities all the norms did cancel
        for (Class<? extends Norm> norm : this.allUsedNorms) {
            processNormCancelled(simulationDay, changedNorms, norm, normCancelled.getOrDefault(norm, new HashMap<>()));
        }

        // Write to files what norms influenced all the activities
        for (ActivityType type : ActivityType.values()) {
            processActivitiesCancelled(simulationDay, changedNorms, type, normCancelled, processed.getValue1().get(type));
        }
    }

    /**
     * Returns a 2-value list, with the first value all the activated norms, and the second value all the
     * deactivated norms for the current simulation day
     *
     * Can be used directly for orderedValues when writing to file
     *
     * @param simulationDay
     * @return
     */
    private List<String> findDayNorms(LocalDate simulationDay) {
        if(!this.normScheduleReader.getEventsMap().containsKey(simulationDay)) {
            return new ArrayList<>(List.of("",""));
        }

        List<String> activated = new ArrayList<>();
        List<String> deactivated = new ArrayList<>();
        for(NormContainer c : this.normScheduleReader.getEventsMap().get(simulationDay)) {
            if(simulationDay.equals(c.getStartDate()) && c.getNorm() != null) {
                activated.add(c.getNorm().toString());
            } else if (simulationDay.equals(c.getEndDate()) && c.getNorm() != null) {
                deactivated.add(c.getNorm().toString());
            }
        }
        return new ArrayList<>(List.of(String.join(",", activated), String.join(",", deactivated)));
    }

    /**
     * Calculate breakout per activity, i.e. what norms influenced each of the activities
     *
     * @param simulationDay
     * @param activityType
     * @param cancelledActivities
     */
    private void processActivitiesCancelled(LocalDate simulationDay,
                                            List<String> normChanges,
                                            ActivityType activityType,
                                            Map<Class<? extends Norm>, Map<ActivityType, Integer>> cancelledActivities,
                                            ConcurrentMap<String, AtomicInteger> visibleAttributes) {

        ScheduleTrackerGroup group = this.fileObjects.get(activityTypeCancelledByNormFile(activityType));
        Map<String, Map<ActivityType, Integer>> stringMap = new HashMap<>();
        for (Class<? extends Norm> norm : cancelledActivities.keySet()) {
            stringMap.put(norm.getSimpleName(), cancelledActivities.get(norm));
        }
        Map<String, Integer> appliedNorms = new HashMap<>();

        for (String header : group.getHeaders()) {
            appliedNorms.put(header, stringMap.getOrDefault(header, new HashMap<>()).getOrDefault(activityType, 0));
        }

        int totalNormsApplied = appliedNorms.values().stream().reduce(Integer::sum).orElse(0);
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE), this.suffix));
        orderedValues.addAll(normChanges);
        orderedValues.add(Integer.toString(visibleAttributes.get("TOTAL").get()));
        for(String visibleAttribute : Constants.VISIBLE_ATTRIBUTES) {
            orderedValues.add(Integer.toString(visibleAttributes.get(visibleAttribute).get()));
        }

        orderedValues.add(Integer.toString(GoalPlanScheme.influencedActivitiesTracker.getCancelledActivities().getOrDefault(activityType, 0)));
        orderedValues.add(Double.toString(GoalPlanScheme.influencedActivitiesTracker.getFractionActivitiesCancelled().getOrDefault(activityType, 0d)));
        for (String header : group.getHeaders()) {
            if (totalNormsApplied > 0) {
                orderedValues.add(Integer.toString(appliedNorms.get(header)));
            } else {
                orderedValues.add("");
            }
        }
        group.writeValuesToFile(orderedValues);
    }

    /**
     * Calculate the breakout per norm, i.e. what activities did each norm influence
     *
     * @param simulationDay
     * @param norm
     * @param cancelledActivities
     */
    private void processNormCancelled(LocalDate simulationDay, List<String> normChanges, Class<? extends Norm> norm, Map<ActivityType, Integer> cancelledActivities) {
        int totalCancelled = cancelledActivities.values().stream().reduce(Integer::sum).orElse(0);
        ScheduleTrackerGroup g = this.fileObjects.get(normToAppliedActivitiesFile(norm));
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE), this.suffix));
        orderedValues.addAll(normChanges);
        orderedValues.add(Integer.toString(totalCancelled));
        orderedValues.add(Double.toString(
                totalCancelled / (double) (GoalPlanScheme.influencedActivitiesTracker.getSumTotalActivities())));

        for (String header : g.getHeaders()) {
            if (totalCancelled > 0 && cancelledActivities.containsKey(ActivityType.valueOf(header))) {
                orderedValues.add(Integer.toString(cancelledActivities.get(ActivityType.valueOf(header))));
            } else {
                orderedValues.add("");
            }
        }
        g.writeValuesToFile(orderedValues);
    }

    /**
     * Calculate the relative proportions of occurrences of each activities for this day. E.g. pct_is_HOME value of
     * .8 means 80% of all activities produced during this day were of type HOME.
     *
     * @param agentActions Actions produced by the agents in this time step
     * @return HashMap containing the relative proportion of occurrences of activities during this
     * time step
     */
    private Pair<Map<String, String>, ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>>> calculateActivityFractions(List<Future<DeliberationResult<CandidateActivity>>> agentActions) {
        AtomicInteger numActivities = new AtomicInteger();
        AtomicLong activityDuration = new AtomicLong();
        AtomicInteger stayedHome = new AtomicInteger();

        AtomicInteger mask = new AtomicInteger();
        AtomicInteger distance = new AtomicInteger();
        AtomicInteger symptomatic = new AtomicInteger();

        ConcurrentMap<ActivityType, AtomicInteger> encounteredActivities = new ConcurrentHashMap<>();
        ConcurrentMap<ActivityType, AtomicLong> encounteredActivityDurations = new ConcurrentHashMap<>();
        ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>> attributePerActivityMap = createVisibleAttributePerActivityMap();

        List<CountVisitorsCallable> callables = new ArrayList<>(this.arguments.getThreads());
        for(int i = 0; i < this.arguments.getThreads(); i++) {
            callables.add(new CountVisitorsCallable(
                    i, this.arguments.getThreads(),
                    agentActions,
                    numActivities, activityDuration, stayedHome,
                    mask, distance, symptomatic,
                    encounteredActivities, encounteredActivityDurations, attributePerActivityMap
            ));
        }

        try {
            this.executor.useExecutorForTasks(callables);
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to process location visits", e);
        }

        Map<String, String> fractions = new HashMap<>();
        fractions.put(VISITORS_TOTAL, Integer.toString(numActivities.get()));
        fractions.put(VISIT_DURATION_TOTAL, Double.toString(activityDuration.get()));
        fractions.put(MASK_TOTAL, Integer.toString(mask.get()));
        fractions.put(DISTANCE_TOTAL, Integer.toString(distance.get()));
        fractions.put(SYMPTOMATIC_TOTAL, Integer.toString(symptomatic.get()));
        fractions.put(MASK_PCT, Double.toString((double) mask.get() / numActivities.get()));
        fractions.put(DISTANCE_PCT, Double.toString((double) distance.get() / numActivities.get()));
        fractions.put(SYMPTOMATIC_PCT, Double.toString((double) symptomatic.get() / numActivities.get()));
        fractions.put(ALL_HOME_PCT, Double.toString((double) stayedHome.get() / agentActions.size()));
        for (ActivityType type : encounteredActivityDurations.keySet()) {
            fractions.put(type.name() + "_DURATION", Double.toString(encounteredActivityDurations.get(type).get()));
            fractions.put(type.name() + "_COUNT", Integer.toString(encounteredActivities.get(type).get()));
        }

        return new Pair<>(fractions, attributePerActivityMap);
    }

    private static class CountVisitorsCallable implements Callable<Void> {
        private final int thread;
        private final int threads_total;
        private final List<Future<DeliberationResult<CandidateActivity>>> agentActions;

        private final AtomicInteger numActivities;
        private final AtomicLong activityDuration;
        private final AtomicInteger stayedHome;

        private final AtomicInteger mask;
        private final AtomicInteger distance;
        private final AtomicInteger symptomatic;

        private final ConcurrentMap<ActivityType, AtomicInteger> encounteredActivities;
        private final ConcurrentMap<ActivityType, AtomicLong> encounteredActivityDurations;
        private final ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>> attributePerActivityMap;

        public CountVisitorsCallable(int thread,
                                     int threads_total,
                                     List<Future<DeliberationResult<CandidateActivity>>> agentActions,
                                     AtomicInteger numActivities,
                                     AtomicLong activityDuration,
                                     AtomicInteger stayedHome,
                                     AtomicInteger mask,
                                     AtomicInteger distance,
                                     AtomicInteger symptomatic,
                                     ConcurrentMap<ActivityType, AtomicInteger> encounteredActivities,
                                     ConcurrentMap<ActivityType, AtomicLong> encounteredActivityDurations,
                                     ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>> attributePerActivityMap) {
            this.thread = thread;
            this.threads_total = threads_total;
            this.agentActions = agentActions;
            this.numActivities = numActivities;
            this.activityDuration = activityDuration;
            this.stayedHome = stayedHome;
            this.mask = mask;
            this.distance = distance;
            this.symptomatic = symptomatic;
            this.encounteredActivities = encounteredActivities;
            this.encounteredActivityDurations = encounteredActivityDurations;
            this.attributePerActivityMap = attributePerActivityMap;
        }

        @Override
        public Void call() throws Exception {
            int start = this.thread * this.agentActions.size() / this.threads_total;
            int end = (thread + 1) * this.agentActions.size() / this.threads_total;
            if (end > this.agentActions.size()) end = this.agentActions.size();

            for(int i = start; i < end; i++) {
                boolean isAllHome = true;
                DeliberationResult<CandidateActivity> result = this.agentActions.get(i).get();
                for (CandidateActivity ca : result.getActions()) {
                    ConcurrentMap<String, AtomicInteger> activityTypeMap = attributePerActivityMap.get(ca.getActivity().getActivityType());
                    numActivities.getAndIncrement();
                    activityDuration.getAndAdd(ca.getActivity().getDuration());
                    activityTypeMap.get("TOTAL").getAndIncrement();
                    if (ca.isMask()) {
                        mask.getAndIncrement();
                        activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_MASK).getAndIncrement();
                    }
                    if (ca.isDistancing()) {
                        distance.getAndIncrement();
                        activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_DISTANCING).getAndIncrement();
                    }
                    if (ca.getDiseaseState().equals(DiseaseState.INFECTED_SYMPTOMATIC)) {
                        symptomatic.getAndIncrement();
                        activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_SYMPTOMATIC).getAndIncrement();
                    }
                    ActivityType type = ca.getActivity().getActivityType();
                    if (!encounteredActivityDurations.containsKey(type)) {
                        encounteredActivityDurations.put(type, new AtomicLong());
                        encounteredActivities.put(type, new AtomicInteger());
                    }
                    encounteredActivityDurations.get(type).addAndGet(ca.getActivity().getDuration());
                    encounteredActivities.get(type).getAndIncrement();
                    isAllHome &= ActivityType.HOME.equals(type);
                }

                if (isAllHome)
                    stayedHome.getAndIncrement();
            }

            return null;
        }
    }

    private Map<String, String> createEpicurveMap() {
        Map<String, Integer> epicurveMap = new HashMap<>();
        for(DiseaseState state : DiseaseState.values()) {
            epicurveMap.put(state.name(), 0);
            for(AgentGroup group : AgentGroup.values()) {
                epicurveMap.put(String.format("%s_%s", group, state.name()), 0);
            }
        }

        for(AgentState state : this.agentStateMap.getAllAgentStates()) {
            epicurveMap.put(state.getState().name(), epicurveMap.get(state.getState().name()) + 1);

            String groupName = String.format("%s_%s", state.getGroup(), state.getState().name());
            epicurveMap.put(groupName, epicurveMap.get(groupName) + 1);
        }

        Map<String, String> epicurveStringMap = new HashMap<>();
        for(String k : epicurveMap.keySet()) {
            epicurveStringMap.put(k, Integer.toString(epicurveMap.get(k)));
        }
        return epicurveStringMap;
    }

    private ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>> createVisibleAttributePerActivityMap() {
        ConcurrentMap<ActivityType, ConcurrentMap<String, AtomicInteger>> m = new ConcurrentHashMap<>();
        for(ActivityType type : ActivityType.values()) {
            m.put(type, new ConcurrentHashMap<>());
            m.get(type).put("TOTAL", new AtomicInteger());
            for(String visibleAttribute : Constants.VISIBLE_ATTRIBUTES) {
                m.get(type).put(visibleAttribute, new AtomicInteger());
            }
        }
        return m;
    }
}
