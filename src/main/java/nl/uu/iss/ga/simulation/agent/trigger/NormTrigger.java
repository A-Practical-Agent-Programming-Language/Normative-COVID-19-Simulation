package main.java.nl.uu.iss.ga.simulation.agent.trigger;

import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;

public class NormTrigger implements Trigger {
    private Norm norm;

    public NormTrigger(Norm norm) {
        this.norm = norm;
    }

    public Norm getNorm() {
        return norm;
    }
}
