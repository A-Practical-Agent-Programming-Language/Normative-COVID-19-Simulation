package nl.uu.iss.ga.pansim.state;

import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.iss.ga.model.disease.AgentGroup;
import nl.uu.iss.ga.model.disease.DiseaseState;

import java.util.Map;
import java.util.Random;

public class AgentState {
    public final static String INITIAL_STATE_HEADERS = "pid,group,start_state";
    private static final String[] INITIAL_STATE_HEADERS_INDICES = INITIAL_STATE_HEADERS.split(ParserUtil.SPLIT_CHAR);

    private long pid;
    private AgentGroup group;
    private DiseaseState state;
    private DiseaseState nextState;
    private int dwell_time;
    private Random random;

    public AgentState(long pid, AgentGroup group, DiseaseState state, DiseaseState nextState, int dwell_time, long seed) {
        this.pid = pid;
        this.group = group;
        this.state = state;
        this.nextState = nextState;
        this.dwell_time = dwell_time;
        this.random = new Random(seed);
    }

    public long getPid() {
        return pid;
    }

    public AgentGroup getGroup() {
        return group;
    }

    public DiseaseState getState() {
        return state;
    }

    public DiseaseState getNextState() {
        return nextState;
    }

    public int getDwell_time() {
        return dwell_time;
    }

    public Random getRandom() {
        return this.random;
    }

    public static AgentState fromCSVLine(String line, int seed) {
        Map<String ,String> keyValue = ParserUtil.zipLine(INITIAL_STATE_HEADERS_INDICES, line);
        return new AgentState(
                ParserUtil.parseAsLong(keyValue.get("pid")),
                CodeTypeInterface.parseAsEnum(AgentGroup.class, keyValue.get("group")),
                CodeTypeInterface.parseAsEnum(DiseaseState.class, keyValue.get("start_state")),
                DiseaseState.NOT_SET,
                -1,
                seed
        );
    }

    void updateState(DiseaseState state) {
        this.state = state;
        this.nextState = DiseaseState.NOT_SET;
        this.dwell_time = -1;
    }

    @Override
    public String toString() {
        return state.toString();
    }
}
