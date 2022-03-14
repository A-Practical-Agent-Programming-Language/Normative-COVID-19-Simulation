package nl.uu.iss.ga.dataframe;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.iss.ga.model.disease.AgentGroup;
import nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.iss.ga.pansim.state.AgentState;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import org.apache.arrow.memory.BufferAllocator;
import org.apache.arrow.memory.RootAllocator;
import org.junit.jupiter.api.BeforeEach;

import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.util.*;

public class TestDataFrame {

    protected static final int TEST_WITH_N_AGENTS = 5000;
    protected final BufferAllocator allocator =  new RootAllocator(Long.MAX_VALUE);
    protected Random random;

    private Map<Long, AgentID> longToAgentUUIDMap;
    private Set<AgentID> previouslyGeneratedAgentIDS;

    @BeforeEach
    void beforeEach() {
        longToAgentUUIDMap = new HashMap<>();
        previouslyGeneratedAgentIDS = new HashSet<>();
        random = new Random();
    }

    protected AgentStateMap createAgentStateMap(long nAgents, long seed) {

        Random random = new Random(seed);

        Map<Long, AgentID> pidToAgentMap = new HashMap<>();
        Map<AgentID, Long> agentToPidMap = new HashMap<>();
        Map<AgentID, Integer> aidCountyCodeMap = new HashMap<>();
        Map<AgentID, AgentState> agentStateMap = new HashMap<>();
        Map<Long, AgentState> pidStateMap = new HashMap<>();

        for(long i = 0; i < nAgents; i++) {
            AgentID aid = getAgentForID(i);

            AgentState agentState = new AgentState(
                    i,
                    getRandomEnumValue(AgentGroup.class, random),
                    getRandomEnumValue(DiseaseState.class, random),
                    getRandomEnumValue(DiseaseState.class, random),
                    random.nextInt(16),
                    random.nextLong()
            );

            pidToAgentMap.put(i, aid);
            agentToPidMap.put(aid, i);
            aidCountyCodeMap.put(aid, 0);
            agentStateMap.put(aid, agentState);
            pidStateMap.put(i, agentState);
        }

        AgentStateMap map = new AgentStateMap(new HashMap<>(), random);
        try {
            setMapField(map, "pidToAgentMap", pidToAgentMap);
            setMapField(map, "agentToPidMap", agentToPidMap);
            setMapField(map, "aidCountyCodeMap", aidCountyCodeMap);
            setMapField(map, "agentStateMap", agentStateMap);
            setMapField(map, "pidStateMap", pidStateMap );
        } catch (IllegalAccessException | NoSuchFieldException e) {
            e.printStackTrace();
        }

        return map;
    }

    protected  <T extends Enum<T>> T getRandomEnumValue(Class<T> enumerator, Random random) {
        T[] options = enumerator.getEnumConstants();
        return options[random.nextInt(options.length-1)];
    }

    protected AgentID getAgentForID(long id) {
        if (this.longToAgentUUIDMap.containsKey(id)) {
            return this.longToAgentUUIDMap.get(id);
        } else {
            AgentID aid = null;
            do {
                try {
                    aid = AgentID.createEmpty();
                } catch (URISyntaxException e) {
                    e.printStackTrace();
                }
            } while (aid == null || previouslyGeneratedAgentIDS.contains(aid));
            this.longToAgentUUIDMap.put(id, aid);
            previouslyGeneratedAgentIDS.add(aid);
            return aid;
        }
    }

    private void setMapField(AgentStateMap map, String fieldName, Map value) throws NoSuchFieldException, IllegalAccessException {
        Field field = map.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);
        field.set(map, value);
    }
}
