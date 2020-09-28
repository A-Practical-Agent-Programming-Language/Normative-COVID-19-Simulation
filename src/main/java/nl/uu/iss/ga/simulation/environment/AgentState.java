package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;

import java.util.Map;
import java.util.Random;

public class AgentState {
    public final static String INITIAL_STATE_HEADERS = "pid,group,start_state";
    private static final String[] INITIAL_STATE_HEADERS_INDICES = INITIAL_STATE_HEADERS.split(ParserUtil.SPLIT_CHAR);

    private long pid;
    private int group;
    private DiseaseState state;
    private DiseaseState nextState;
    private int dwell_time;
    private Random random;

    public AgentState(long pid, int group, DiseaseState state, DiseaseState nextState, int dwell_time, long seed) {
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

    public int getGroup() {
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
                ParserUtil.parseAsInt(keyValue.get("group")),
                CodeTypeInterface.parseAsEnum(DiseaseState.class, keyValue.get("start_state")),
                null,
                0,
                seed
        );
    }
}
