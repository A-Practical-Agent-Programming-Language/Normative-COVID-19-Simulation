package main.java.nl.uu.iss.ga.simulation.agent.trigger;

import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;

public class DiseaseStateUpdatedTrigger implements Trigger {

    private final long tick;
    private final int grace_period; // Some sort of value that says when it migrates to next state
    private final DiseaseState newDiseaseState;
    private final boolean symptomatic;

    public DiseaseStateUpdatedTrigger(long tick, int grace_period, DiseaseState newDiseaseState, boolean symptomatic) {
        this.tick = tick;
        this.grace_period = grace_period;
        this.newDiseaseState = newDiseaseState;
        this.symptomatic = symptomatic;
    }

    public long getTick() {
        return tick;
    }

    public int getGrace_period() {
        return grace_period;
    }

    public DiseaseState getNewDiseaseState() {
        return newDiseaseState;
    }

    public boolean isSymptomatic() {
        return symptomatic;
    }
}
