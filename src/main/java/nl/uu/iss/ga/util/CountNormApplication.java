package main.java.nl.uu.iss.ga.util;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.pansim.state.AgentStateMap;
import main.java.nl.uu.iss.ga.simulation.EnvironmentInterface;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Agent;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;

import java.io.*;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Utility class to count the number of agents throughout the simulation to which a norm applies
 */
public class CountNormApplication {

    private static final Logger LOGGER = Logger.getLogger(CountNormApplication.class.getSimpleName());

    private final Map<String, Integer> agentsAffected = new HashMap<>();
    private final Map<String, Long> timeAffected = new HashMap<>();
    private final Platform platform;
    private final EnvironmentInterface environmentInterface;
    private final ArgParse arguments;
    private final AgentStateMap agentStateMap;
    private final Map<String, List<Norm>> norms;

    private final File outputFile;

    public CountNormApplication(
            Platform platform,
            ArgParse arguments,
            EnvironmentInterface environmentInterface,
            AgentStateMap agentStateMap,
            Map<String, List<Norm>> norms
    ) {
        this.platform = platform;
        this.arguments = arguments;
        this.environmentInterface = environmentInterface;
        this.agentStateMap = agentStateMap;
        this.norms = norms;
        this.outputFile = getFileName();

        // Artificially pretend 7 days already went by, so location history becomes active
        this.environmentInterface.tickPreHook(7);

        prepareNormCount();

        Map<Long, List<Activity>> groupedActivities = groupActivitiesByLocation();
        List<Callable<Void>> callables = new ArrayList<>();
        List<Long> locationIds = new ArrayList<>(groupedActivities.keySet());
        for(int i = 0; i < arguments.getThreads(); i++) {
            callables.add(new GroupedActivityHistoryContextAdder(locationIds, groupedActivities, i, arguments.getThreads()));
        }
        try {
            this.platform.getTickExecutor().useExecutorForTasks(callables);
            LOGGER.log(Level.INFO, String.format("Processed all visits using %d threads", arguments.getThreads()));
        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Failed to process visits", e);
        }

        countAffectedAgents();
    }

    @SuppressWarnings("rawtypes")
    private void countAffectedAgents() {
        Map<AgentID, Agent> agents = platform.getAgents();
        for(Agent<CandidateActivity> agent : agents.values()) {
            AgentContextInterface<CandidateActivity> agentContextInterface = new AgentContextInterface<>(agent);
            List<Activity> activities = getAgentActivities(agent);
            agentContextInterface.getContext(LocationHistoryContext.class).setLastDayTick(8);
            for(String normString : norms.keySet()) {
                long duration = normAffectsAgent(agentContextInterface, activities, norms.get(normString));
                if(duration > 0) {
                    this.incrementNorm(normString, duration);
                }
            }
        }
    }

    /**
     * Populates map of norms and counts (initially set to 0)
     */
    private void prepareNormCount() {
        for(String normString : this.norms.keySet()) {
            this.agentsAffected.put(normString, 0);
            this.timeAffected.put(normString, 0L);
        }
    }

    /**
     * Increment count for a specific norm
     * @param normString String denoting norm-param pair
     */
    private void incrementNorm(String normString, long duration) {
        this.agentsAffected.put(normString, this.agentsAffected.get(normString) + 1);
        this.timeAffected.put(normString, this.timeAffected.get(normString) + duration);
    }

    /**
     * Extract the default activities the agent pursues on a weekly basis
     * @param agent Agent
     * @return      List of activities
     */
    private List<Activity> getAgentActivities(Agent<CandidateActivity> agent) {
        List<Activity> activities = new ArrayList<>();
        for(Goal goal : agent.getGoals()) {
            if (goal instanceof Activity) {
                activities.add((Activity) goal);
            }
        }
        return activities;
    }

    /**
     * Group all activities of all agents by the location at which they take place
     * @return Map with location ID as key and list of activities taking place at that location as value
     */
    private Map<Long, List<Activity>> groupActivitiesByLocation() {
        Map<Long, List<Activity>> activities = new HashMap<>();
        for(Agent<CandidateActivity> agent : this.platform.getAgents().values()) {
            for(Activity activity : getAgentActivities(agent)) {
                if(!activities.containsKey(activity.getLocation().getLocationID())) {
                    activities.put(activity.getLocation().getLocationID(), new ArrayList<>());
                }
                activities.get(activity.getLocation().getLocationID()).add(activity);
            }
        }

        LOGGER.log(Level.INFO, String.format("Grouped activities into %d locations", activities.size()));

        Comparator<Activity> comparator = Comparator.comparingInt(activity -> activity.getStart_time().getSeconds());

        for(Long locationID : activities.keySet()) {
            activities.get(locationID).sort(comparator);
        }

        LOGGER.log(Level.INFO, "Sorted activities by start date");

        return activities;
    }

    private class GroupedActivityHistoryContextAdder implements Callable<Void> {

        private final List<Long> locationIds;
        private final Map<Long, List<Activity>> groupedVisits;
        private final int thread;
        private final int threads_total;

        public GroupedActivityHistoryContextAdder(List<Long> locationIds, Map<Long, List<Activity>> groupedVisits, int thread, int threads_total) {
            this.locationIds = locationIds;
            this.groupedVisits = groupedVisits;
            this.thread = thread;
            this.threads_total = threads_total;
        }

        @Override
        public Void call() throws Exception {
            int start = this.thread * this.locationIds.size() / this.threads_total;
            int end = (thread + 1) * this.locationIds.size() / this.threads_total;
            if (end > this.locationIds.size()) end = this.locationIds.size();

            for(int l = start; l < end; l++) {
                Long locationId = this.locationIds.get(l);
                List<Activity> locationVisits = groupedVisits.get(locationId);

                for(int i = 0; i < locationVisits.size(); i++) {
                    Activity firstActivity = locationVisits.get(i);
                    int n_contacts = 0;
                    for (int j = 0; j < locationVisits.size(); j++) {
                        Activity secondActivity = locationVisits.get(j);
                        // TODO: Can be optimized better, if we just start at a later point in the list
                        if(firstActivity.overlaps(secondActivity)) {
                            n_contacts += 1;
                        } else if (secondActivity.getStartTime() > firstActivity.getEndTime()) {
                            // All following activities start after the first activity ends, no need to continue J-loop
                            break;
                        }
                    }
                    LocationHistoryContext.Visit visit = new LocationHistoryContext.Visit(
                            firstActivity.getPid(), firstActivity.getLocation().getLocationID(), 0,
                            n_contacts, 0, 0, 0
                    );
                    AgentID aid = agentStateMap.getPidToAgentMap().get(firstActivity.getPid());
                    Agent<CandidateActivity> agent = CountNormApplication.this.platform.getAgents().get(aid);
                    LocationHistoryContext context = agent.getContext(LocationHistoryContext.class);

                    context.addVisit(firstActivity.getStart_time().getDayOfWeek().getCode(), visit);
                }
            }
            LOGGER.log(Level.INFO, "Finished adding history contexts to agents on thread " + this.thread);

            return null;
        }
    }

    private long normAffectsAgent(AgentContextInterface<CandidateActivity> agentContextInterface, List<Activity> agentActivities, List<Norm> norms) {
        long totalDuration = 0;
        for(Activity activity : agentActivities) {
            for(Norm norm : norms) {
                if (norm.applicable(activity, agentContextInterface)) {
                    totalDuration += activity.getDuration();
                }
            }
        }
        return totalDuration;
    }

    public void writeAffectedAgentsToFile() {
        Methods.createOutputFile(this.outputFile);
        try(
                FileOutputStream fos = new FileOutputStream(this.outputFile);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        ) {
            bw.write("norm;affected;duration\n");
            for(String norm : this.agentsAffected.keySet()) {
                bw.write(String.format("%s;%d;%d\n", norm, this.agentsAffected.get(norm), this.timeAffected.get(norm)));
            }
            LOGGER.log(Level.INFO, "Created " + this.outputFile.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(
                    Level.SEVERE,
                    "Failed to write number of affected agents for each norm to file " + this.outputFile.getAbsolutePath(),
                    e
            );
        }
    }

    private File getFileName() {
        List<Integer> fipsCodes = new ArrayList<>();
        for(ConfigModel countyConfig : this.arguments.getCounties()) {
            fipsCodes.add(countyConfig.getFipsCode());
        }
        Collections.sort(fipsCodes);
        String[] fipsCodeStrings = new String[fipsCodes.size()];
        for(int i = 0; i < fipsCodes.size(); i++) {
            fipsCodeStrings[i] = Integer.toString(fipsCodes.get(i));
        }
        String fileName = String.format(
                "affected-agents-per-norm-%s.csv",
                String.join("-", fipsCodeStrings)
        );
        return new File(arguments.getOutputDir(), fileName);
    }

}
