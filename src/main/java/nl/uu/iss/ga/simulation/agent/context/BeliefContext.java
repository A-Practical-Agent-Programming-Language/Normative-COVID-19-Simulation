package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.simulation.agent.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

/**
 * Stores agents general beliefs
 */
public class BeliefContext implements Context {
    private final EnvironmentInterface environmentInterface;

    public BeliefContext(EnvironmentInterface environmentInterface) {
        this.environmentInterface = environmentInterface;
    }

    public EnvironmentInterface getEnvironmentInterface() {
        return environmentInterface;
    }
}
