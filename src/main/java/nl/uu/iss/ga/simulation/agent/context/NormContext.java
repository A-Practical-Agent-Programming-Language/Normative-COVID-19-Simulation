package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.ArrayList;
import java.util.List;

/**
 * Contains the agents beliefs regarding institutional norms,
 * which can be classified as regimented or non-regimented.
 */
public class NormContext implements Context {

    private List<Norm> regimentedNorms = new ArrayList<>();
    private List<NonRegimentedNorm> nonRegimentedNorms = new ArrayList<>();

    /**
     * Add a new norm and set the agent's attitude towards that norm
     *
     * @param norm      Norm
     */
    public void addNorm(Norm norm) {
        this.regimentedNorms.add(norm);
    }

    public void addNorm(NonRegimentedNorm norm) {
        this.nonRegimentedNorms.add(norm);
    }

    public <T extends Norm> T replaceNorm(T newNorm) {
        T removed = null;
        for(Norm norm : this.regimentedNorms) {
            if(norm.getClass().equals(newNorm.getClass())) {
                removed = (T) norm;
                this.regimentedNorms.remove(norm);
                break;
            }
        }
        addNorm(newNorm);
        return removed;
    }

    public <T extends NonRegimentedNorm> T replaceNorm(T newNorm) {
        T removed = null;
        for(Norm norm : this.nonRegimentedNorms) {
            if(norm.getClass().equals(newNorm.getClass())) {
                removed = (T) norm;
                this.nonRegimentedNorms.remove(norm);
                break;
            }
        }
        addNorm(newNorm);
        return removed;
    }

    public List<Norm> getNorms() {
        ArrayList<Norm> norms = new ArrayList<>(this.regimentedNorms);
        norms.addAll(this.nonRegimentedNorms);
        return norms;
    }

    public List<Norm> getRegimentedNorms() {
        return new ArrayList<>(regimentedNorms);
    }

    public List<NonRegimentedNorm> getNonRegimentedNorms() {
        return new ArrayList<>(nonRegimentedNorms);
    }
}
