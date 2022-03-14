package nl.uu.iss.ga.pansim.state;

import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.disease.AgentGroup;
import nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.iss.ga.util.tracking.ScheduleTrackerGroup;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentStateMap {

    private static final Logger LOGGER = Logger.getLogger(AgentStateMap.class.getName());
    private static final String SEEDED_AGENTS_FILE_NAME = "seeded-agents.csv";

    private final Map<Long, AgentID> pidToAgentMap = new HashMap<>();
    private final Map<AgentID, Long> agentToPidMap = new HashMap<>();
    private final Map<AgentID, Integer> aidCountyCodeMap = new HashMap<>();
    private Map<AgentID, AgentState> agentStateMap;
    private Map<Long, AgentState> pidStateMap;

    private ScheduleTrackerGroup seededAgentsGroup;

    private AgentStateMap() {
        reset();
    }

    public AgentStateMap(List<File> initialStateFiles, Random rnd) {
        reset();
        for(File f : initialStateFiles) {
            LOGGER.log(Level.INFO, "Creating agent state map from file " + initialStateFiles);
            readStateFile(f, rnd);
        }
    }

    public void seed_infections(String date, Random random, int n_agents) {
        List<AgentID> allAgents = new ArrayList<>(this.agentStateMap.keySet());
        List<AgentID> seeded = new ArrayList<>();
        Collections.shuffle(allAgents, random);
        for(int i = 0; i < n_agents && i < this.agentStateMap.size(); i++) {
            AgentState agentState = this.agentStateMap.get(allAgents.get(i));
            if(DiseaseState.SUSCEPTIBLE == agentState.getState() || DiseaseState.NOT_SET == agentState.getState()) {
                this.agentStateMap.get(allAgents.get(i)).updateState(DiseaseState.EXPOSED);
                seeded.add(allAgents.get(i));
            } else {
                n_agents++;
            }
        }
        
        if(this.seededAgentsGroup != null) {
            for(AgentID aid : seeded) {
                this.seededAgentsGroup.writeLineToFile(String.format("%s;%d%s", date, this.agentToPidMap.get(aid), System.lineSeparator()));
            }
        }
    }

    public AgentStateMap(Map<Long, Person> personMap, Random rnd) {
        reset();
        for(long pid : personMap.keySet()) {
            AgentState state = new AgentState(pid, AgentGroup.fromPerson(personMap.get(pid)), DiseaseState.SUSCEPTIBLE, DiseaseState.NOT_SET, -1, rnd.nextInt());
            this.pidStateMap.put(pid, state);
        }
    }

    public void fromDataframe(StateDataFrame dataFrame, int threads, TickExecutor<CandidateActivity> executor) {
        List<Callable<Vector<Integer>>> callables = new ArrayList<>();
        for(int i = 0; i < threads; i++) {
            callables.add(new FromDataFrameCallable(dataFrame, threads, i));
        }

        int infected = 0;
        int symptomatic = 0;
        int total = 0;

        try {
            List<Future<Vector<Integer>>> futures = executor.useExecutorForTasks(callables);

            for(int i = 0; i < futures.size(); i++) {
                infected += futures.get(i).get().get(0);
                symptomatic += futures.get(i).get().get(1);
                total += futures.get(i).get().get(2);
            }

            LOGGER.log(Level.INFO, String.format(
                    "Received state frame. %d people, of whom %d (%.2f%%) are infected, %d (%.2f%%) of which are symptomatic",
                    total,
                    infected, (double)infected/total * 100,
                    symptomatic, (double) symptomatic/infected * 100
            ));
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }
    }

    class FromDataFrameCallable implements Callable<Vector<Integer>> {

        private final StateDataFrame dataFrame;
        private final int totalThreads;
        private final int thread;

        public FromDataFrameCallable(StateDataFrame dataFrame, int totalThreads, int thread) {
            this.dataFrame = dataFrame;
            this.totalThreads = totalThreads;
            this.thread = thread;
        }

        @Override
        public Vector<Integer> call() throws Exception {
            int infected = 0;
            int symptomatic = 0;
            int total = 0;

            int start = thread * dataFrame.getSchemaRoot().getRowCount() / totalThreads;
            int end = (thread + 1) * dataFrame.getSchemaRoot().getRowCount() / totalThreads;
            if (end > dataFrame.getSchemaRoot().getRowCount()) end = dataFrame.getSchemaRoot().getRowCount();

            try {
                for (int i = start; i < end; i++) {
                    AgentState state = dataFrame.getAgentState(i);
                    AgentID aid = pidToAgentMap.get(state.getPid());
                    pidStateMap.put(state.getPid(), state);
                    agentStateMap.put(aid, state);
                    total++;
                    if (state.getState().equals(DiseaseState.INFECTED_SYMPTOMATIC)) {
                        symptomatic++;
                        infected++;
                    } else if (state.getState().equals(DiseaseState.INFECTED_ASYMPTOMATIC)) {
                        infected++;
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.exit(430);
            }

            Vector<Integer> v = new Vector<>();
            v.add(infected);
            v.add(symptomatic);
            v.add(total);

            return v;
        }
    }

    public List<AgentState> getAllAgentStates() {
        if(this.pidStateMap == null) {
            LOGGER.log(Level.SEVERE, "PID to State map is null! This should not be possible");
        }
        if(this.agentStateMap == null) {
            LOGGER.log(Level.SEVERE, "AgentID to State map is nulL! This should not be possible");
        }
        if(this.pidStateMap != null && this.agentStateMap != null && this.agentStateMap.size() !=  this.pidStateMap.size()) {
            LOGGER.log(Level.SEVERE, "PID to state and AID to state map sizes differ!");
            LOGGER.log(Level.SEVERE, String.format("PID map is %d big, AID map is %d big", this.pidStateMap.size(), this.agentStateMap.size()));
        }
        return new ArrayList<>(this.pidStateMap.values());
    }

    public int getNumberOfStates() {
        return this.pidStateMap.size();
    }

    public void addAgent(AgentID aid, long pid, int fipsCode) {
        if(this.pidToAgentMap.containsKey(pid)) {
            LOGGER.log(Level.WARNING, String.format(
                "Adding PID %d to agentID %s, but PID is already used", pid, aid.toString()
            ));
        }
        this.pidToAgentMap.put(pid, aid);
        if(this.agentToPidMap.containsKey(aid)) {
            LOGGER.log(Level.WARNING, String.format(
                    "Adding agent %s with PID %d, but agentID is already used.", aid.toString(), pid));
        }
        this.agentToPidMap.put(aid, pid);
        this.aidCountyCodeMap.put(aid, fipsCode);

        this.agentStateMap.put(aid, this.pidStateMap.get(pid));
    }

    public Random getRandom(AgentID agentID) {
        if(this.agentStateMap == null || !this.agentStateMap.containsKey(agentID)) {
            List<String> messages = new ArrayList<>(Arrays.asList("Failed to get Random object for agent " + agentID));
            if(this.agentStateMap == null) {
                messages.add("Agent state map is null");
            } else {
                messages.add(String.format(
                        "Agent state map has size %d but does not contain key %s",
                        this.agentStateMap.size(),
                        agentID.toString()
                ));
                String pid2aidSize = this.pidToAgentMap == null ? "null" : Integer.toString(this.pidToAgentMap.size());
                String pid2stateSize = this.pidStateMap == null ? "null" : Integer.toString(this.pidStateMap.size());
                String aid2pidSize = "null";

                if(this.agentToPidMap != null) {
                    aid2pidSize = Integer.toString(this.agentToPidMap.size());
                    if(this.agentToPidMap.containsKey(agentID)) {
                        long pid = this.agentToPidMap.get(agentID);
                        messages.add(String.format("AgentID %s is associated with PID %d", agentID, pid));
                        if(this.pidToAgentMap != null && this.pidToAgentMap.containsKey(pid)) {
                            messages.add(String.format(
                                    "AgentID %s associated with %d maps back to AgentID %s",
                                    agentID, pid, this.pidToAgentMap.get(pid))
                            );
                        } else if (this.pidToAgentMap != null) {
                            messages.add(String.format(
                                    "AgentID %s is associated with PID %d but this PID does not map back to any agent",
                                    agentID, pid
                            ));
                        }
                    } else {
                        messages.add("AgentID " + agentID + " is not in the aid2pid map");
                    }
                }

                messages.add(String.format("pid2state size %s\tpidToAid size %s\taidToPid size %s", pid2stateSize, pid2aidSize, aid2pidSize));
            }
            for(String str : messages) {
                LOGGER.log(Level.SEVERE, str);
            }
        }
        return getAgentState(agentID).getRandom();
    }

    public boolean isSymptomatic(AgentID agentID) {
        return getDiseaseState(agentID).equals(DiseaseState.INFECTED_SYMPTOMATIC);
    }

    public DiseaseState getDiseaseState(AgentID agentID) {
        return getAgentState(agentID).getState();
    }

    public AgentState getAgentState(AgentID agentID) {
        return this.agentStateMap.get(agentID);
    }

    public AgentState getAgentState(Long pid) {
        return this.pidStateMap.get(pid);
    }

    public int getFipsCode(AgentID agentID) {
        return this.aidCountyCodeMap.get(agentID);
    }

    public Map<Long, AgentID> getPidToAgentMap() {
        return new HashMap<>(pidToAgentMap);
    }

    public Map<AgentID, Long> getAgentToPidMap() {
        return new HashMap<>(agentToPidMap);
    }

    private void readStateFile(File stateFile, Random rnd) {
        try (
                FileInputStream is = new FileInputStream(stateFile);
                Scanner s = new Scanner(is);
        ) {
            iterateStates(s, rnd);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read state file " + stateFile.toString(), e);
        }
    }

    private void iterateStates(Scanner s, Random rnd) {
        while(s.hasNextLine()) {
            String line = s.nextLine();
            AgentState state = AgentState.fromCSVLine(line, rnd.nextInt());
            this.pidStateMap.put(state.getPid(), state);
        }
    }

    private void reset() {
        this.agentStateMap = new HashMap<>();
        this.pidStateMap = new HashMap<>();
    }

    public static AgentStateMap merge(File outputDir, boolean suppressCalculations, List<AgentStateMap> maps) {
        AgentStateMap merged = new AgentStateMap();
        merged.seededAgentsGroup = suppressCalculations ? null :
                new ScheduleTrackerGroup(outputDir, SEEDED_AGENTS_FILE_NAME, Collections.singletonList("AgentID"));
        for(AgentStateMap map : maps) {
            merged.pidStateMap.putAll(map.pidStateMap);
            merged.agentStateMap.putAll(map.agentStateMap);
            merged.pidToAgentMap.putAll(map.pidToAgentMap);
            merged.agentToPidMap.putAll(map.agentToPidMap);
            merged.aidCountyCodeMap.putAll(map.aidCountyCodeMap);
        }
        return merged;
    }

    /****
     * THE FOLLOWING IS FOR TESTING PURPOSES ONLY AND SHOULD NEVER BE INVOKED IN PRODUCTION
     */
    private AgentState removedState;
    private AgentID removedAgent;

    public void removeRandomAgent() {
        // Remove random agent, see if we can reproduce issue on compute cluster
        List<AgentID> ids = new ArrayList<>(this.agentStateMap.keySet());
        int rnd = (int) Math.round(Math.random() * ids.size());
        this.removedAgent = ids.get(rnd);
        this.removedState = this.agentStateMap.remove(this.removedAgent);
        LOGGER.log(Level.SEVERE, "Removed agent " + this.removedAgent + " from agent state map. FOR TESTING PURPOSES ONLY! Do not forget to restore!!!");
    }

    public void restoreRandomAgent() {
        this.agentStateMap.put(this.removedAgent, this.removedState);
        LOGGER.log(Level.SEVERE, "Restored agent " + this.removedAgent + ". FOR TESTING PURPOSES ONLY! Remove method calls in production");
        this.removedAgent = null;
        this.removedState = null;
    }
}
