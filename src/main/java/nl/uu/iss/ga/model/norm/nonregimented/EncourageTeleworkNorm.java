package nl.uu.iss.ga.model.norm.nonregimented;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.CandidateActivity;
import nl.uu.iss.ga.model.data.Household;
import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.data.dictionary.Designation;
import nl.uu.iss.ga.model.factor.AccomodatedToWorkFromHome;
import nl.uu.iss.ga.model.factor.FractionSymptomatic;
import nl.uu.iss.ga.model.factor.IFactor;
import nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;

import java.util.Arrays;
import java.util.List;

/**
 * This norm encourages agents to work from home, without enforcing it.
 * Various studies show only a fraction of people have a type of job that
 * accommodates working from home, and that people with those jobs tend to
 * earn higher wages.
 *
 * Note that this reasoning process is applied every time an agent considers to go to work,
 * instead of having a decision on a per-agent basis of whether their job accommodates
 * working from home.
 */
public class EncourageTeleworkNorm extends NonRegimentedNorm {

    //https://www.pewresearch.org/fact-tank/2020/03/20/before-the-coronavirus-telework-was-an-optional-benefit-mostly-for-the-affluent-few/
    // https://www.bls.gov/opub/mlr/2020/article/ability-to-work-from-home.htm
    public static final double pct_accomodated_work_from_home = .45;

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);
        Person person = agentContextInterface.getContext(Person.class);
        Household household = person.getHousehold();

        // Factors
        double gtf = agentContextInterface.getContext(BeliefContext.class).getPriorTrustAttitude();

        // How many symptomatic people were there at the office previously
        double fractionSymptomatic = historyContext.getLastDaysFractionSymptomaticAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

        return NonRegimentedNorm.norm_violation_posterior(gtf, fractionSymptomatic, pct_accomodated_work_from_home);

        // TODO Household income as proxy for how likely they can work from home?

//
//        // Weights
//        double gtfWeight = weight(gtf);
//        double fsWeight = weight(fractionSymptomatic);
//        double paWeight = weight(probabilityAccomodated);
//
//        return ((gtfWeight * gtf) + (fsWeight * fractionSymptomatic) + (paWeight * probabilityAccomodated)) /
//                (gtfWeight + fsWeight + paWeight);

    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        Designation designation = agentContextInterface.getContext(Person.class).getDesignation();
        return activity.getActivityType().equals(ActivityType.WORK) &&
                designation.equals(Designation.none);
    }

    @Override
    public String toString() {
        return String.format("EncourageTelework[lookback=%dDays,pct_able=%.2f]", N_DAYS_LOOKBACK, pct_accomodated_work_from_home);
    }

    @Override
    public List<IFactor> getFactors() {
        return Arrays.asList(
                new AccomodatedToWorkFromHome(),
                new FractionSymptomatic()
        );
    }
}
