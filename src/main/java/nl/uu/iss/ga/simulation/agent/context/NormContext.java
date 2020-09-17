package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Contains the agents beliefs regarding institutional norms,
 * which can be classified as regimented or non-regimented.
 */
public class NormContext implements Context {

    private Map<Norm, Double> attitudeTowardsNormMap = new HashMap<>();

    /**
     * Add a new norm and set the agent's attitude towards that norm
     *
     * @param norm      Norm
     * @param attitude  Attitude value between -3 and 3
     */
    public void addNorm(Norm norm, double attitude) {
        if(attitude < -3 || attitude > 3) {
            throw new IllegalArgumentException("Attitude must be value between -3 and 3. Got " + attitude);
        }
        this.attitudeTowardsNormMap.put(norm, attitude);
    }

    public double getAttitudeForNorm(Norm norm) {
        return this.attitudeTowardsNormMap.get(norm);
    }

    public List<Norm> getActiveNorms() {
        return new ArrayList<>(this.attitudeTowardsNormMap.keySet());
    }
}
