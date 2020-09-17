package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class WearMaskNorm extends Norm {

    public static final String NAME = "NORM_WEAR_MASK";

    public WearMaskNorm() {
        super(NAME, NORM_TYPE.OBLIGATION, false);
    }

    @Override
    public CandidateActivity applyTo(CandidateActivity activity, AgentContextInterface agentContextInterface) {
        return activity.clone().setMask(true).setNormApplied(this);
    }
}
