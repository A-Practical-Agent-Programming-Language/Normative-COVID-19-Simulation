package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.pansim.state.StateDataFrame;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class AgentStateMap {

    private Map<Long, AgentID> pidToAgentMap;
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
        this.pidToAgentMap = new HashMap<>();
        for(long pid : personMap.keySet()) {
            AgentState state = new AgentState(pid, 0, DiseaseState.SUSCEPTIBLE, null, 0, rnd.nextInt());
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

    public void addAgent(AgentID aid, long pid) {
        if(this.pidToAgentMap.containsKey(pid)) {
            System.err.printf("Adding agent to PID %d, but agent already exists%n", pid);
        }
        this.pidToAgentMap.put(pid, aid);
        this.agentStateMap.put(aid, this.pidStateMap.get(pid));
    }

    private void readStateFile(File stateFile, Random rnd) {
        try (
                FileInputStream is = new FileInputStream(stateFile);
                Scanner s = new Scanner(is);
        ) {
            iterateStates(s, rnd);
        } catch (IOException e) {
            e.printStackTrace();
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
}
