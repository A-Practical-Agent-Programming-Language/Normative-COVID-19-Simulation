package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.simulation.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

/**
 * Stores agents general beliefs
 */
public class BeliefContext implements Context {
    private final EnvironmentInterface environmentInterface;
    private final double governmentTrustFactor;
    private final LocationEntry homeLocation;

    public BeliefContext(EnvironmentInterface environmentInterface, LocationEntry homeLocation,
                         double governmentTrustFactor) {
        this.environmentInterface = environmentInterface;
        this.homeLocation = homeLocation;
        this.governmentTrustFactor = governmentTrustFactor;
    }

    public EnvironmentInterface getEnvironmentInterface() {
        return environmentInterface;
    }

    public double getGovernmentTrustFactor() {
        return governmentTrustFactor;
    }

    public LocationEntry getHomeLocation() {
        return homeLocation;
    }
}
