package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.ArrayList;
import java.util.List;

public class CandidateActivity implements Cloneable {

    private Activity activity;
    private RiskMitigationPolicy riskMitigationPolicy = RiskMitigationPolicy.NONE;
    private DiseaseState diseaseState = DiseaseState.SUSCEPTIBLE;

    List<Norm> appliedNorms = new ArrayList<>();

    public CandidateActivity(Activity activity) {
        this.activity = activity.clone();
    }

    public Activity getActivity() {
        return activity;
    }

    public void setActivity(Activity activity) {
        this.activity = activity;
    }

    public boolean isMask() {
        return this.riskMitigationPolicy.isMask();
    }

    public CandidateActivity setMask(boolean mask) {
        if(mask) {
            this.riskMitigationPolicy = this.riskMitigationPolicy.isDistance() ? RiskMitigationPolicy.BOTH : RiskMitigationPolicy.DISTANCING;
        } else {
            this.riskMitigationPolicy = this.riskMitigationPolicy.isDistance() ? RiskMitigationPolicy.DISTANCING : RiskMitigationPolicy.NONE;
        }
        return this;
    }

    public boolean isDistancing() {
        return this.riskMitigationPolicy.isDistance();
    }

    public CandidateActivity setDistancing(boolean distancing) {
        if(distancing) {
            this.riskMitigationPolicy = this.riskMitigationPolicy.isMask() ? RiskMitigationPolicy.BOTH : RiskMitigationPolicy.MASK;
        } else {
            this.riskMitigationPolicy = this.riskMitigationPolicy.isMask() ? RiskMitigationPolicy.MASK : RiskMitigationPolicy.NONE;
        }
        return this;
    }

    public CandidateActivity setNormApplied(Norm norm) {
        this.appliedNorms.add(norm);
        return this;
    }

    public List<Norm> getAppliedNorms() {
        return appliedNorms;
    }

    public RiskMitigationPolicy getRiskMitigationPolicy() {
        return riskMitigationPolicy;
    }

    public void setRiskMitigationPolicy(RiskMitigationPolicy riskMitigationPolicy) {
        this.riskMitigationPolicy = riskMitigationPolicy;
    }

    @Override
    public CandidateActivity clone() {
        CandidateActivity clone = new CandidateActivity(this.activity);
        clone.riskMitigationPolicy = this.riskMitigationPolicy;
        clone.appliedNorms = new ArrayList<>(this.appliedNorms);
        return clone;
    }

    public DiseaseState getDiseaseState() {
        return diseaseState;
    }

    public void setDiseaseState(DiseaseState diseaseState) {
        this.diseaseState = diseaseState;
    }

    @Override
    public String toString() {
        // start_time, duration, location, mask_state, disease_state
        return String.format(
                "%s    - %s ; %s",
                this.getActivity(),
                this.riskMitigationPolicy,
                this.diseaseState
        );
    }

    public void makeHome(AgentContextInterface<CandidateActivity> agentContextInterface) {
        makeHome(agentContextInterface, DetailedActivity.REGULAR_HOME_ACTIVITIES);
    }

    public void makeHome(AgentContextInterface<CandidateActivity> agentContextInterface, DetailedActivity detailedActivity) {
        activity.setLocation(agentContextInterface.getContext(BeliefContext.class).getHomeLocation());
        activity.setActivityType(ActivityType.HOME);
        activity.setDetailed_activity(detailedActivity);
    }

}
