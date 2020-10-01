package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AgentStateMap {

    private static final Logger LOGGER = Logger.getLogger(AgentStateMap.class.getName());

    private final Map<Long, AgentID> pidToAgentMap = new ConcurrentHashMap<>();
    private final Map<AgentID, Long> agentToPidMap = new ConcurrentHashMap<>();
    private Map<AgentID, AgentState> agentStateMap;
    private Map<Long, AgentState> pidStateMap;

    public AgentStateMap(List<File> initialStateFiles, Random rnd) {
        reset();
        for(File f : initialStateFiles) {
            readStateFile(f, rnd);
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
        for(int i = 0; i < dataFrame.getSchemaRoot().getRowCount(); i++) {
            AgentState state = dataFrame.getAgentState(i);
            AgentID aid = this.pidToAgentMap.get(state.getPid());
            this.pidStateMap.put(state.getPid(), state);
            this.agentStateMap.put(aid, state);
        }
    }

    public List<AgentState> getAllAgentStates() {
        return new ArrayList<>(this.pidStateMap.values());
    }

    public int getNumberOfStates() {
        return this.pidStateMap.size();
    }

    public void addAgent(AgentID aid, long pid) {
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
        this.agentStateMap = new ConcurrentHashMap<>();
        this.pidStateMap = new ConcurrentHashMap<>();
    }
}
