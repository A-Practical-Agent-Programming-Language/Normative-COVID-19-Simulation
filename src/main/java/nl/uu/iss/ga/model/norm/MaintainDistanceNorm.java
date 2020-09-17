package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class MaintainDistanceNorm extends Norm {

    public static final String NAME = "NORM_MAINTAIN_DISTANCE";

    public MaintainDistanceNorm() {
        super(NAME, NORM_TYPE.OBLIGATION, false);
        this.setParameter("distance", "1.5m");
    }

    @Override
    public CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface) {
        return activity.clone().setNormApplied(this).setDistancing(true);
    }
}
