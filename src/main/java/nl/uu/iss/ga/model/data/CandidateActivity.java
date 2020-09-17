package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.Norm;

import java.util.ArrayList;
import java.util.List;

public class CandidateActivity implements Cloneable {

    private Activity activity;
    private boolean mask = false;
    private boolean distancing = false;

    List<Norm> appliedNorms = new ArrayList<>();

    public CandidateActivity() {

    }

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
        return mask;
    }

    public CandidateActivity setMask(boolean mask) {
        this.mask = mask;
        return this;
    }

    public boolean isDistancing() {
        return distancing;
    }

    public CandidateActivity setDistancing(boolean distancing) {
        this.distancing = distancing;
        return this;
    }

    public CandidateActivity setNormApplied(Norm norm) {
        this.appliedNorms.add(norm);
        return this;
    }

    public List<Norm> getAppliedNorms() {
        return appliedNorms;
    }

    @Override
    public CandidateActivity clone() {
        CandidateActivity clone = new CandidateActivity();
        clone.activity = this.activity.clone();
        clone.mask = this.mask;
        clone.distancing = this.distancing;
        clone.appliedNorms = new ArrayList<>(this.appliedNorms);
        return clone;
    }

    @Override
    public String toString() {
        // start_time, duration, location, mask_state, disease_state
        return String.format(
                "%s;%d;%d;%s;%s",
                this.getActivity().getStart_time().getArmy_time_of_day(),
                this.getActivity().getDuration(),
                this.getActivity().getLocation().getLocationID(),
                RiskMitigationPolicy.NONE,
                DiseaseState.SUSCEPTIBLE
        );
    }

}
