package main.java.nl.uu.iss.ga.model.norm.nonregimented;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Household;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.model.norm.NonRegimentedNorm;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

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

    public static final int N_DAYS_LOOKBACK = 10;

    //https://www.pewresearch.org/fact-tank/2020/03/20/before-the-coronavirus-telework-was-an-optional-benefit-mostly-for-the-affluent-few/
    // https://www.bls.gov/opub/mlr/2020/article/ability-to-work-from-home.htm
    public static final double pct_accomodated_work_from_home = .45;

    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        LocationHistoryContext historyContext = agentContextInterface.getContext(LocationHistoryContext.class);
        Person person = agentContextInterface.getContext(Person.class);
        Household household = person.getHousehold();



        // Factors
        double gtf = 1 - agentContextInterface.getContext(BeliefContext.class).getGovernmentTrustFactor();

        // How many symptomatic people were there at the office previously
        double fractionSymptomatic = 1 - historyContext.getLastDaysFractionSymptomaticAt(N_DAYS_LOOKBACK, activity.getLocation().getLocationID());

        // How likely is it any given person in the work force can work from home
        double probabilityAccomodated = pct_accomodated_work_from_home;

        // TODO Household income as proxy for how likely they can work from home?


        // Weights
        double gtfWeight = weight(gtf);
        double fsWeight = weight(fractionSymptomatic);
        double paWeight = weight(probabilityAccomodated);

        return ((gtfWeight * gtf) + (fsWeight * fractionSymptomatic) + (paWeight * probabilityAccomodated)) /
                (gtfWeight + fsWeight + paWeight);

    }

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return null;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return activity.getActivityType().equals(ActivityType.WORK) &&
                agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.none);
    }
}
