package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class AgentStateMap {

    private final Map<Long, AgentID> pidToAgentMap;
    private Map<AgentID, AgentState> agentStateMap;
    private Map<Long, AgentState> pidStateMap;

    public AgentStateMap(List<File> initialStateFiles, Random rnd) {
        reset();
        this.pidToAgentMap = new HashMap<>();
        for(File f : initialStateFiles) {
            readStateFile(f, rnd);
        }
    }

    public AgentStateMap(Map<Long, Person> personMap, Random rnd) {
        reset();
        this.pidToAgentMap = new ConcurrentHashMap<>();
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
            Platform.getLogger().log(getClass(), Level.WARNING, String.format(
                "WARNING: Adding agent to PID %d, but agent already exists", pid
            ));
        }
        this.pidToAgentMap.put(pid, aid);
        this.agentStateMap.put(aid, this.pidStateMap.get(pid));
    }

    public Random getRandom(AgentID agentID) {
        return this.agentStateMap.get(agentID).getRandom();
    }

    public boolean isSymptomatic(AgentID agentID) {
        return this.agentStateMap.get(agentID).getState().equals(DiseaseState.INFECTED_SYMPTOMATIC);
    }

    private void readStateFile(File stateFile, Random rnd) {
        try (
                FileInputStream is = new FileInputStream(stateFile);
                Scanner s = new Scanner(is);
        ) {
            iterateStates(s, rnd);
        } catch (IOException e) {
            Platform.getLogger().log(getClass(), e);
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
