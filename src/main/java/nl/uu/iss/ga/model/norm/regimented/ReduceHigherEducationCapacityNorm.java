package nl.uu.iss.ga.model.norm.regimented;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.GradeLevel;
import nl.uu.iss.ga.model.norm.Norm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

@Deprecated
// This norm only becomes active after our simulation ends, so not useful to look at it
public class ReduceHigherEducationCapacityNorm extends Norm {

    // Just a guess?
    public final double FRACTION_COURSES_ONLINE = .8;

    // From regulation, see below
    public final int MAX_ALLOWED = 50;

    public final int N_DAYS_LOOKBACK = 14; // Used to be 21; // three weeks sounds reasonable?

    /*
    Institutions of higher education are encouraged to continue remote learning where practical. However, such institutions may offer
    in-person classes and instruction, including labs and related practical training, provided they comply with all applicable
    requirements under the "Guidelines for All Business Sectors." No institutions of higher education shall hold or host gatherings of
    more than 50 individuals. Any post secondary provider offering vocational training in a profession regulated by a Virginia
    state agency/board must also comply with any sector-specific guidelines relevant to that profession to the extent possible
    under the regulatory training requirements. Such professions may include, but are not necessarily limited to:
    aesthetician, barber,cosmetologist, massage therapist, nail technician, and practical nurse
     */

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        boolean isSchool = activity.getActivityType().equals(ActivityType.SCHOOL);
        GradeLevel level = agentContextInterface.getContext(Person.class).getGrade_level();
        boolean isHigherEducation = level != null && level.isHigher();
        boolean isTooManyPeople = agentContextInterface.getContext(LocationHistoryContext.class).getLastDaysSeenAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID()) > MAX_ALLOWED;
        boolean isOnlineCourse = agentContextInterface.getContext(BeliefContext.class).getRandom().nextDouble() < FRACTION_COURSES_ONLINE;

        return isSchool && isHigherEducation & (isTooManyPeople || isOnlineCourse);
    }

    @Override
    public String toString() {
        return "ReduceHigherEducationCapacityNorm[" +
                "fractionOnline=" + FRACTION_COURSES_ONLINE +
                ", max=" + MAX_ALLOWED +
                ", lookback=" + N_DAYS_LOOKBACK +
                ']';
    }
}
