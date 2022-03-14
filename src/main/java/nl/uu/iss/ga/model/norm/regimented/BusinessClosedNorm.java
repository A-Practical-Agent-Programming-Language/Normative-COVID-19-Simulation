package nl.uu.iss.ga.model.norm.regimented;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;

/**
 * When the government issues a closure of all businesses, we cancel all work-related activities for people
 * who are not in the essential work force.
 */
public class BusinessClosedNorm extends Norm {

    private APPLIES appliesTo;

    public BusinessClosedNorm(String parameter) {
        if(parameter.contains("NEB"))
            this.appliesTo = APPLIES.NONESSENTIALBUSINESSES;
        else if(parameter.contains("DMV"))
            this.appliesTo = APPLIES.DMV;
        else
            this.appliesTo = APPLIES.NONE;
    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        // TODO:
        //  if a N.E.B. is closed, workers should stay home. But we have no way of knowing the job of the agent?
        //  Can we use the designations in the 1.9.0 located activity files? If it has designation retail, but the worker
        //  is not classified as essential, the worker stays home?

        if(APPLIES.DMV.equals(this.appliesTo)) {
            return activity.getLocation().getDesignation().equals(Designation.dmv);
        } else if(!Arrays.asList(ActivityType.SHOP, ActivityType.OTHER).contains(activity.getActivityType())) {
            return false;
        } else if (APPLIES.NONESSENTIALBUSINESSES.equals(this.appliesTo)) {
            return Designation.none.equals(activity.getLocation().getDesignation()) && !activity.getLocation().isResidential();
        } else {
            return false;
        }
    }

    public APPLIES getAppliesTo() {
        return appliesTo;
    }

    enum APPLIES {
        NONE,
        NONESSENTIALBUSINESSES,
        DMV;
    }

    @Override
    public String toString() {
        return String.format("BusinessesClosed[%s]", appliesTo);
    }
}
