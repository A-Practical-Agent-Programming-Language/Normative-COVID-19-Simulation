package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

/**
 * Contains the agents beliefs regarding the spread of
 * disease, and the general perceived risk of its peers, and similar
 * beliefs the agent uses to reason about perceived risk.
 */
public class DiseaseRiskContext implements Context {

    private boolean symptomatic = false;

    private DiseaseState state = DiseaseState.SUSCEPTIBLE;

    public boolean isSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(boolean symptomatic) {
        this.symptomatic = symptomatic;
    }

    /**
     * WARNING: Should not be used in agent reasoning
     * @return
     */
    public DiseaseState getState() {
        return state;
    }

    /**
     * WARNING: Should only be set after environment update
     * @param state
     */
    public void setState(DiseaseState state) {
        this.state = state;
    }
}
