package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.simulation.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.Random;

/**
 * Stores agents general beliefs
 */
public class BeliefContext implements Context {
    private AgentID me;
    private final EnvironmentInterface environmentInterface;
    private final double governmentTrustFactor;
    private final LocationEntry homeLocation;

    public BeliefContext(
            EnvironmentInterface environmentInterface,
            LocationEntry homeLocation,
            double governmentTrustFactor
    ) {
        this.environmentInterface = environmentInterface;
        this.homeLocation = homeLocation;
        this.governmentTrustFactor = governmentTrustFactor;
    }

    public void setAgentID(AgentID me) {
        this.me = me;
    }

    public DayOfWeek getToday() {
        return this.environmentInterface.getToday();
    }

    public long getCurrentTick() {
        return this.environmentInterface.getCurrentTick();
    }

    public Random getRandom() {
        return this.environmentInterface.getRnd(this.me);
    }

    public double getGovernmentTrustFactor() {
        return governmentTrustFactor;
    }

    public LocationEntry getHomeLocation() {
        return homeLocation;
    }

    public boolean isSymptomatic() {
        return this.environmentInterface.isSymptomatic(this.me);
    }
}
