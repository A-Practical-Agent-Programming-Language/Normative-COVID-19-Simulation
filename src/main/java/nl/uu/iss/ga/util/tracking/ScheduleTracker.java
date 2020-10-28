package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.planscheme.GoalPlanScheme;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

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
    private final Map<String, ScheduleTrackerGroup> fileObjects = new HashMap<>();
    private final String parentDir;
    private final Set<Class<? extends Norm>> allUsedNorms;

    public ScheduleTracker(String outdir, Set<Class<? extends Norm>> allUsedNorms) {
        this.parentDir = (Path.of(outdir).isAbsolute() ? Path.of(outdir) : Path.of("output", outdir)).toFile().getAbsolutePath();
        this.allUsedNorms = allUsedNorms;
        createFileObjects(allUsedNorms);
    }

    private void createFileObjects(Set<Class<? extends Norm>> allUsedNorms) {
        List<String> activityTypeNames = Arrays.stream(ActivityType.values())
                .filter(x -> !ActivityType.TRIP.equals(x))
                .map(ActivityType::name).collect(Collectors.toList());

        List<String> averageScheduleHeaders = new ArrayList<>(Collections.singletonList(ALL_HOME_PCT));
        averageScheduleHeaders.addAll(activityTypeNames);
        ScheduleTrackerGroup g = new ScheduleTrackerGroup(this.parentDir, AVERAGE_SCHEDULE_FILENAME + ".csv", averageScheduleHeaders);
        this.fileObjects.put(AVERAGE_SCHEDULE_FILENAME, g);

        for(Class<? extends Norm> norm : allUsedNorms) {
                this.fileObjects.put(
                        normToAppliedActivitiesFile(norm),
                        new ScheduleTrackerGroup(this.parentDir,
                                normToAppliedActivitiesFile(norm) + ".csv",
                                activityTypeNames,
                                "total_cancelled",
                                "fraction_cancelled"));
        }

        List<String> activityTypeHeaders = allUsedNorms.stream().map(Class::getSimpleName).collect(Collectors.toList());
        for(ActivityType activityType : ActivityType.values()) {
            this.fileObjects.put(
                activityTypeCancelledByNormFile(activityType),
                    new ScheduleTrackerGroup(this.parentDir,
                            activityTypeCancelledByNormFile(activityType) + ".csv",
                            activityTypeHeaders,
                            "total_cancelled","fraction_cancelled")
            );
        }
    }

    static String normToAppliedActivitiesFile(Class<? extends Norm> norm) {
        return String.format("per_activity_breakout_%s", norm.getSimpleName());
    }

    static String activityTypeCancelledByNormFile(ActivityType type) {
        return String.format("cancelled_per_norm_breakout_%s", type.name());
    }

    /**
     * Write all the changes to the activity schedules produced by the agent in the last timestep to the output
     * file.
     *
     * @param simulationDay Date of the current simulation day
     * @param agentActions  Actions produced by the agents during this simulation day
     */
    public void processTick(LocalDate simulationDay, HashMap<AgentID, List<CandidateActivity>> agentActions) {
        // Write to files what percentage of each type of activity was cancelled
        this.fileObjects.get(AVERAGE_SCHEDULE_FILENAME).writeKeyMapToFile(simulationDay, calculateActivityFractions(agentActions));

        Map<Class<? extends Norm>, Map<ActivityType, Integer>> normCancelled =
                GoalPlanScheme.influencedActivitiesTracker.getActivitiesCancelledByNorm();

        // Write to files what activities all the norms did cancel
        for(Class<? extends Norm> norm : this.allUsedNorms) {
            processNormCancelled(simulationDay, norm, normCancelled.getOrDefault(norm, new HashMap<>()));
        }

        // Write to files what norms influenced all the activities
        for(ActivityType type : ActivityType.values()) {
            processActivitiesCancelled(simulationDay, type, normCancelled);
        }
    }

    /**
     * Calculate breakout per activity, i.e. what norms influenced each of the activities
     * @param simulationDay
     * @param activityType
     * @param cancelledActivities
     */
    private void processActivitiesCancelled(LocalDate simulationDay, ActivityType activityType, Map<Class<? extends Norm>, Map<ActivityType, Integer>> cancelledActivities) {
        ScheduleTrackerGroup group = this.fileObjects.get(activityTypeCancelledByNormFile(activityType));
        Map<String, Map<ActivityType, Integer>> stringMap = new HashMap<>();
        for(Class<? extends Norm> norm : cancelledActivities.keySet()) {
            stringMap.put(norm.getSimpleName(), cancelledActivities.get(norm));
        }
        Map<String, Integer> appliedNorms = new HashMap<>();

        for(String header : group.getHeaders()) {
            appliedNorms.put(header, stringMap.getOrDefault(header, new HashMap<>()).getOrDefault(activityType, 0));
        }

        int totalNormsApplied = appliedNorms.values().stream().reduce(Integer::sum).orElse(0);
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE)));
        orderedValues.add(Integer.toString(GoalPlanScheme.influencedActivitiesTracker.getCancelledActivities().getOrDefault(activityType, 0)));
        orderedValues.add(Double.toString(GoalPlanScheme.influencedActivitiesTracker.getFractionActivitiesCancelled().getOrDefault(activityType, 0d)));
        for(String header : group.getHeaders()) {
            if(totalNormsApplied > 0) {
                orderedValues.add(Double.toString((double)appliedNorms.get(header) / totalNormsApplied));
            } else {
                orderedValues.add("");
            }
        }
        group.writeValuesToFile(orderedValues);
    }

    /**
     * Calculate the breakout per norm, i.e. what activities did each norm influence
     * @param simulationDay
     * @param norm
     * @param cancelledActivities
     */
    private void processNormCancelled(LocalDate simulationDay, Class<? extends Norm> norm, Map<ActivityType, Integer> cancelledActivities) {
        int totalCancelled = cancelledActivities.values().stream().reduce(Integer::sum).orElse(0);
        ScheduleTrackerGroup g = this.fileObjects.get(normToAppliedActivitiesFile(norm));
        List<String> orderedValues = new ArrayList<>(List.of(simulationDay.format(DateTimeFormatter.ISO_DATE)));

        orderedValues.add(Integer.toString(totalCancelled));
        orderedValues.add(Double.toString(
                totalCancelled / (double) (GoalPlanScheme.influencedActivitiesTracker.getSumTotalActivities())));

        for(String header : g.getHeaders()) {
            if (totalCancelled > 0 && cancelledActivities.containsKey(ActivityType.valueOf(header))) {
                orderedValues.add(Double.toString((double) cancelledActivities.get(ActivityType.valueOf(header)) / totalCancelled));
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
     * @param agentActions  Actions produced by the agents in this time step
     * @return              HashMap containing the relative proportion of occurrences of activities during this
     *                      time step
     */
    private Map<String, String> calculateActivityFractions(HashMap<AgentID, List<CandidateActivity>> agentActions) {
        int numActivities = 0;
        int stayedHome = 0;
        Map<ActivityType, Integer> encounteredActivities = new HashMap<>();

        for(List<CandidateActivity> agentActivities : agentActions.values()) {
            boolean isAllHome = true;
            for(CandidateActivity ca : agentActivities) {
                numActivities++;
                ActivityType type = ca.getActivity().getActivityType();
                if(!encounteredActivities.containsKey(type))
                    encounteredActivities.put(type, 1);
                encounteredActivities.put(type, encounteredActivities.get(type) + 1);
                isAllHome &= ActivityType.HOME.equals(type);
            }

            if(isAllHome)
                stayedHome++;
        }

        Map<String, String> fractions = new HashMap<>();
        fractions.put(ALL_HOME_PCT, Double.toString((double) stayedHome / agentActions.size()));
        for(ActivityType type : encounteredActivities.keySet()) {
            fractions.put(type.name(), Double.toString((double) encounteredActivities.get(type) / numActivities));
        }

        return fractions;
    }

    private Map<String, String> calculateVisibleAttributes() {
        Map<String, String> visibleAttributeMap = new HashMap<>();

        return visibleAttributeMap;
    }
}
