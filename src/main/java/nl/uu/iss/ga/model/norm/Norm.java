package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.HashMap;
import java.util.Map;

public abstract class Norm {

    private final String name;
    private final NORM_TYPE type;
    private final boolean regimented;
    private Map<String, Object> parameters = new HashMap<>();

    public Norm(String name, NORM_TYPE type) {
        this.name = name;
        this.type = type;
        this.regimented = false;
    }

    protected Norm(String name, NORM_TYPE type, boolean regimented) {
        this.name = name;
        this.type = type;
        this.regimented = regimented;
    }

    public String getName() {
        return name;
    }

    public NORM_TYPE getType() {
        return type;
    }

    public boolean isRegimented() {
        return regimented;
    }

    public enum NORM_TYPE {
        OBLIGATION,
        PROHIBITION;
    }

    protected void setParameter(String name, Object value) {
        this.parameters.put(name, value);
    }

    public Object getParameter(String name) {
        return this.parameters.get(name);
    }

    public HashMap<String, Object> getParameters() {
        return new HashMap<>(this.parameters);
    }

    public abstract CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface);
}
