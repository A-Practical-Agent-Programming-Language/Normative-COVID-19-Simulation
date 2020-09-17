package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.simulation.agent.environment.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

/**
 * Stores agents general beliefs
 */
public class BeliefContext implements Context {
    private final EnvironmentInterface environmentInterface;
    private double governmentTrustFactor;
    private LocationEntry homeLocation;

    public BeliefContext(EnvironmentInterface environmentInterface, LocationEntry homeLocation) {
        this.environmentInterface = environmentInterface;
        this.homeLocation = homeLocation;
    }

    public EnvironmentInterface getEnvironmentInterface() {
        return environmentInterface;
    }

    public double getGovernmentTrustFactor() {
        return governmentTrustFactor;
    }

    public void setGovernmentTrustFactor(double governmentTrustFactor) {
        this.governmentTrustFactor = governmentTrustFactor;
    }

    public LocationEntry getHomeLocation() {
        return homeLocation;
    }
}
