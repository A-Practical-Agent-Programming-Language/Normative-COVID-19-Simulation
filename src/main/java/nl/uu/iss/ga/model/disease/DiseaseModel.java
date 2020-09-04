package main.java.nl.uu.iss.ga.model.disease;

import main.java.nl.uu.iss.ga.simulation.agent.environment.EnvironmentInterface;

public class DiseaseModel {
    private RiskMitigationPolicy policy;
    private DiseaseState diseaseState;

    private EnvironmentInterface environmentInterface;

    private long lastDiseaseStateUpdate = 0;

    public DiseaseModel(EnvironmentInterface environmentInterface) {
        this.policy = RiskMitigationPolicy.NONE;
        this.diseaseState = DiseaseState.SUSCEPTIBLE;
    }

    public RiskMitigationPolicy getPolicy() {
        return policy;
    }

    /**
     * The agent is free to change the policy whenever they want to, for each activity that they choose
     * @param policy    New risk mitigation policy adopted by agent
     */
    public void setPolicy(RiskMitigationPolicy policy) {
        this.policy = policy;
    }

    public DiseaseState getDiseaseState() {
        return diseaseState;
    }

    /**
     * The disease state is not known to the agent, and may only be changed by the environment (in the case of the
     * transition from susceptible to exposed this is based on interaction, in the other cases, this is a stocahstic
     * approach)
     * @param diseaseState
     */
    public void setDiseaseState(DiseaseState diseaseState) {
        this.diseaseState = diseaseState;
        this.lastDiseaseStateUpdate = environmentInterface.getCurrentTick();
    }
}
