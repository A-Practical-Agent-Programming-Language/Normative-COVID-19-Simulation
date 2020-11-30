package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.model.norm.NormContainer;
import main.java.nl.uu.iss.ga.model.reader.NormScheduleReader;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import main.java.nl.uu.iss.ga.util.Constants;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import org.javatuples.Pair;

import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Class to keep track of changes in schedule due to applied norms
 */
public class ScheduleTracker {
    public static final String AVERAGE_SCHEDULE_FILENAME = "average-schedules";
    public static final String ALL_HOME_PCT = "ALL_HOME_PCT";
    public static final String VISITORS_TOTAL = "VISITORS_TOTAL";
    public static final String MASK_TOTAL = "MASK_TOTAL";
    public static final String DISTANCE_TOTAL = "DISTANCE_TOTAL";
    public static final String SYMPTOMATIC_TOTAL = "SYMPTOMATIC_TOTAL";
    public static final String MASK_PCT = "MASK_PCT";
    public static final String DISTANCE_PCT = "DISTANCE_PCT";
    public static final String SYMPTOMATIC_PCT = "SYMPTOMATIC_PCT";
    public static final String NORMS_ACTIVATED_HEADER = "NORMS_ACTIVATED";
    public static final String NORMS_DEACTIVATED_HEADER = "NORMS_DEACTIVATED";
    private final Map<String, ScheduleTrackerGroup> fileObjects = new HashMap<>();
    private final String parentDir;
    private final NormScheduleReader normScheduleReader;
    private final Set<Class<? extends Norm>> allUsedNorms;

    public ScheduleTracker(String outdir, NormScheduleReader normScheduleReader) {
        this.parentDir = (Path.of(outdir).isAbsolute() ? Path.of(outdir) : Path.of("output", outdir)).toFile().getAbsolutePath();
        this.normScheduleReader = normScheduleReader;
        this.allUsedNorms = normScheduleReader.getAllUsedNorms();
        createFileObjects(allUsedNorms);
    }

    static String normToAppliedActivitiesFile(Class<? extends Norm> norm) {
        return String.format("per_activity_breakout_%s", norm.getSimpleName());
    }

    static String activityTypeCancelledByNormFile(ActivityType type) {
        return String.format("cancelled_per_norm_breakout_%s", type.name());
    }

    private void createFileObjects(Set<Class<? extends Norm>> allUsedNorms) {
        List<String> activityTypeNames = Arrays.stream(ActivityType.values())
                .filter(x -> !ActivityType.TRIP.equals(x))
                .map(ActivityType::name).collect(Collectors.toList());

        List<String> averageScheduleHeaders = new ArrayList<>(List.of(
                NORMS_ACTIVATED_HEADER, NORMS_DEACTIVATED_HEADER,
                VISITORS_TOTAL, MASK_TOTAL, DISTANCE_TOTAL, SYMPTOMATIC_TOTAL, MASK_PCT, DISTANCE_PCT, SYMPTOMATIC_PCT, ALL_HOME_PCT));
        averageScheduleHeaders.addAll(activityTypeNames);
        ScheduleTrackerGroup g =
                new ScheduleTrackerGroup(this.parentDir, AVERAGE_SCHEDULE_FILENAME + ".csv", averageScheduleHeaders);
        this.fileObjects.put(AVERAGE_SCHEDULE_FILENAME, g);

        for (Class<? extends Norm> norm : allUsedNorms) {
            this.fileObjects.put(
                    normToAppliedActivitiesFile(norm),
                    new ScheduleTrackerGroup(this.parentDir,
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
                    new ScheduleTrackerGroup(this.parentDir,
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
    public void processTick(LocalDate simulationDay, HashMap<AgentID, List<CandidateActivity>> agentActions) {
        List<String> changedNorms = findDayNorms(simulationDay);

        // Write to files what percentage of each type of activity was cancelled
        Pair<Map<String,String>, Map<ActivityType, Map<String, Integer>>> processed = calculateActivityFractions(agentActions);
        Map<String,String> averageScheduleMap = processed.getValue0();
        averageScheduleMap.put(NORMS_ACTIVATED_HEADER, changedNorms.get(0));
        averageScheduleMap.put(NORMS_DEACTIVATED_HEADER, changedNorms.get(1));
        this.fileObjects.get(AVERAGE_SCHEDULE_FILENAME).writeKeyMapToFile(simulationDay, averageScheduleMap);

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
                                            Map<String, Integer> visibleAttributes) {

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
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE)));
        orderedValues.addAll(normChanges);
        orderedValues.add(Integer.toString(visibleAttributes.get("TOTAL")));
        for(String visibleAttribute : Constants.VISIBLE_ATTRIBUTES) {
            orderedValues.add(Integer.toString(visibleAttributes.get(visibleAttribute)));
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
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE)));
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
    private Pair<Map<String, String>, Map<ActivityType, Map<String, Integer>>> calculateActivityFractions(HashMap<AgentID, List<CandidateActivity>> agentActions) {
        int numActivities = 0;
        int stayedHome = 0;

        int mask = 0;
        int distance = 0;
        int symptomatic = 0;

        Map<ActivityType, Integer> encounteredActivities = new HashMap<>();
        Map<ActivityType, Map<String, Integer>> attributePerActivityMap = createVisibleAttributePerActivityMap();

        for (List<CandidateActivity> agentActivities : agentActions.values()) {
            boolean isAllHome = true;
            for (CandidateActivity ca : agentActivities) {
                Map<String, Integer> activityTypeMap = attributePerActivityMap.get(ca.getActivity().getActivityType());
                numActivities++;
                activityTypeMap.put("TOTAL", activityTypeMap.get("TOTAL") + 1);
                if (ca.isMask()) {
                    mask++;
                    activityTypeMap.put(Constants.VISIBLE_ATTRIBUTE_MASK, activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_MASK) + 1);
                }
                if (ca.isDistancing()) {
                    distance++;
                    activityTypeMap.put(Constants.VISIBLE_ATTRIBUTE_DISTANCING, activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_DISTANCING) + 1);
                }
                if (ca.getDiseaseState().equals(DiseaseState.INFECTED_SYMPTOMATIC)) {
                    symptomatic++;
                    activityTypeMap.put(Constants.VISIBLE_ATTRIBUTE_SYMPTOMATIC, activityTypeMap.get(Constants.VISIBLE_ATTRIBUTE_SYMPTOMATIC) + 1);
                }
                ActivityType type = ca.getActivity().getActivityType();
                if (!encounteredActivities.containsKey(type))
                    encounteredActivities.put(type, ca.getActivity().getDuration());
                encounteredActivities.put(type, encounteredActivities.get(type) + ca.getActivity().getDuration());
                isAllHome &= ActivityType.HOME.equals(type);
            }

            if (isAllHome)
                stayedHome++;
        }

        Map<String, String> fractions = new HashMap<>();
        fractions.put(VISITORS_TOTAL, Integer.toString(numActivities));
        fractions.put(MASK_TOTAL, Integer.toString(mask));
        fractions.put(DISTANCE_TOTAL, Integer.toString(distance));
        fractions.put(SYMPTOMATIC_TOTAL, Integer.toString(symptomatic));
        fractions.put(MASK_PCT, Double.toString((double) mask / numActivities));
        fractions.put(DISTANCE_PCT, Double.toString((double) distance / numActivities));
        fractions.put(SYMPTOMATIC_PCT, Double.toString((double) symptomatic / numActivities));
        fractions.put(ALL_HOME_PCT, Double.toString((double) stayedHome / agentActions.size()));
        for (ActivityType type : encounteredActivities.keySet()) {
            fractions.put(type.name(), Double.toString((double) encounteredActivities.get(type) / numActivities));
        }

        return new Pair<>(fractions, attributePerActivityMap);
    }

    private Map<ActivityType, Map<String, Integer>> createVisibleAttributePerActivityMap() {
        Map<ActivityType, Map<String, Integer>> m = new HashMap<>();
        for(ActivityType type : ActivityType.values()) {
            m.put(type, new HashMap<>());
            m.get(type).put("TOTAL", 0);
            for(String visibleAttribute : Constants.VISIBLE_ATTRIBUTES) {
                m.get(type).put(visibleAttribute, 0);
            }
        }
        return m;
    }
}
