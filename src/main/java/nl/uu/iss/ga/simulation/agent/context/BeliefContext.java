package nl.uu.iss.ga.simulation.agent.context;

import nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import nl.uu.iss.ga.simulation.PansimEnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.Random;

/**
 * Stores agents general beliefs
 */
public class BeliefContext implements Context {
    private AgentID me;
    private final PansimEnvironmentInterface pansimEnvironmentInterface;
    private double priorTrustAttitude;
    private final LocationEntry homeLocation;

    public BeliefContext(
            PansimEnvironmentInterface pansimEnvironmentInterface,
            LocationEntry homeLocation,
            double priorTrustAttitude
    ) {
        this.pansimEnvironmentInterface = pansimEnvironmentInterface;
        this.homeLocation = homeLocation;
        this.priorTrustAttitude = priorTrustAttitude;
    }

    public void setAgentID(AgentID me) {
        this.me = me;
    }

    public DayOfWeek getToday() {
        return this.pansimEnvironmentInterface.getToday();
    }

    public long getCurrentTimeStep() {
        return this.pansimEnvironmentInterface.getCurrentTimeStep();
    }

    public Random getRandom() {
        return this.pansimEnvironmentInterface.getRnd(this.me);
    }

    public double getPriorTrustAttitude() {
        return priorTrustAttitude;
    }

    public void setPriorTrustAttitude(double priorTrustAttitude) {
        this.priorTrustAttitude = priorTrustAttitude;
    }

    public LocationEntry getHomeLocation() {
        return homeLocation;
    }

    public boolean isSymptomatic() {
        return this.pansimEnvironmentInterface.isSymptomatic(this.me);
    }
}
