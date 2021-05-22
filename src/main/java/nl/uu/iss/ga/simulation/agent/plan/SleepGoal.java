package main.java.nl.uu.iss.ga.simulation.agent.plan;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

public class SleepGoal extends Goal {

    private final long sleepTime;

    public SleepGoal(long sleepTime) {
        this.sleepTime = sleepTime;
    }

    public long getSleepTime() {
        return sleepTime;
    }

    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        return false;
    }
}
