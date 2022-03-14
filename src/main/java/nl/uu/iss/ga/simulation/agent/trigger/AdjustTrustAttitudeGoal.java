package nl.uu.iss.ga.simulation.agent.trigger;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

public class AdjustTrustAttitudeGoal extends Goal {

    private final double fatigueFactor;
    private final long fatigueStart;

    public AdjustTrustAttitudeGoal(double fatigueFactor, long fatigueStart) {
        this.fatigueFactor = fatigueFactor;
        this.fatigueStart = fatigueStart;
    }

    public double getFatigueFactor() {
        return fatigueFactor;
    }

    public long getFatigueStart() {
        return fatigueStart;
    }

    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        return false;
    }
}
