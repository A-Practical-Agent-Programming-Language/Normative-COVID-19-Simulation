package main.java.nl.uu.iss.ga.simulation.agent.trigger;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

public class AdjustHAITrustGoal extends Goal {

    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        // Maintenance goal; never achieved
        return false;
    }
}
