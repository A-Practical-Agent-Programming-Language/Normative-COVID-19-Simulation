package main.java.nl.uu.iss.ga.model.norm;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Trigger;

public class NormNotificationTrigger implements Trigger {

    private Norm norm;

    public NormNotificationTrigger(Norm norm) {
        this.norm = norm;
    }

    public Norm getNorm() {
        return norm;
    }
}
