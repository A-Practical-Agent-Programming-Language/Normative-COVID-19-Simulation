package main.java.nl.uu.iss.ga.pansim.state;

import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentStateMap {

    private static final Logger LOGGER = Logger.getLogger(AgentStateMap.class.getName());

    private final Map<Long, AgentID> pidToAgentMap = new HashMap<>();
    private final Map<AgentID, Long> agentToPidMap = new HashMap<>();
    private final Map<AgentID, Integer> aidCountyCodeMap = new HashMap<>();
    private Map<AgentID, AgentState> agentStateMap;
    private Map<Long, AgentState> pidStateMap;

    private AgentStateMap() {
        reset();
    }

    public AgentStateMap(List<File> initialStateFiles, Random rnd) {
        reset();
        for(File f : initialStateFiles) {
            LOGGER.log(Level.INFO, "Creating agent state map from file " + initialStateFiles.toString());
            readStateFile(f, rnd);
        }
    }

    public void seed_infections(Random random, int n_agents, double symptomatic_probability) {
        List<AgentID> allAgents = new ArrayList<>(this.agentStateMap.keySet());
        Collections.shuffle(allAgents, random);
        for(int i = 0; i < n_agents && i < this.agentStateMap.size(); i++) {
            AgentState agentState = this.agentStateMap.get(allAgents.get(i));
            if(DiseaseState.SUSCEPTIBLE == agentState.getState() || DiseaseState.NOT_SET == agentState.getState()) {
                this.agentStateMap.get(allAgents.get(i)).updateState(random.nextDouble() <= symptomatic_probability ?
                        DiseaseState.INFECTED_SYMPTOMATIC : DiseaseState.INFECTED_ASYMPTOMATIC);
            } else {
                n_agents++;
            }
        }
    }

    public AgentStateMap(Map<Long, Person> personMap, Random rnd) {
        reset();
        for(long pid : personMap.keySet()) {
            AgentState state = new AgentState(pid, 0, DiseaseState.SUSCEPTIBLE, DiseaseState.NOT_SET, -1, rnd.nextInt());
            this.pidStateMap.put(pid, state);
        }
    }

    public void fromDataFrame(StateDataFrame dataFrame) throws IOException {
        reset();
        int infected = 0;
        int symptomatic = 0;
        int total = 0;
        for(int i = 0; i < dataFrame.getSchemaRoot().getRowCount(); i++) {
            AgentState state = dataFrame.getAgentState(i);
            AgentID aid = this.pidToAgentMap.get(state.getPid());
            this.pidStateMap.put(state.getPid(), state);
            this.agentStateMap.put(aid, state);
            total++;
            if(state.getState().equals(DiseaseState.INFECTED_SYMPTOMATIC)) {
                symptomatic++;
                infected++;
            } else if (state.getState().equals(DiseaseState.INFECTED_ASYMPTOMATIC)) {
                infected++;
            }
        }
        // TODO maybe track this in tracker.InfluencedActivities?
        LOGGER.log(Level.INFO, String.format(
                "Received state frame. %d people, of whom %d (%.2f%%) are infected, %d (%.2f%%) of which are symptomatic",
                total,
                infected, (double)infected/total * 100,
                symptomatic, (double) symptomatic/infected * 100
                ));
    }

    public List<AgentState> getAllAgentStates() {
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

    public static AgentStateMap merge(List<AgentStateMap> maps) {
        AgentStateMap merged = new AgentStateMap();
        for(AgentStateMap map : maps) {
            merged.pidStateMap.putAll(map.pidStateMap);
            merged.agentStateMap.putAll(map.agentStateMap);
            merged.pidToAgentMap.putAll(map.pidToAgentMap);
            merged.agentToPidMap.putAll(map.agentToPidMap);
            merged.aidCountyCodeMap.putAll(map.aidCountyCodeMap);
        }
        return merged;
    }
}
