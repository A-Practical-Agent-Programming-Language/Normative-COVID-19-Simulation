package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.model.factor.FractionMask;
import main.java.nl.uu.iss.ga.model.factor.IFactor;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

import java.util.Arrays;
import java.util.List;

/**
 * This is not technically a norm, but a government provision that allows the wearing of masks, which was
 * previously prohibitied.
 *
 * We use, like with all norms, the attitude towards the government as a general proxy for how people will behave
 * to be safe. Some people will be more inclined to wear masks in public settings, even before this is required
 * by law
 */
public class AllowWearMaskNorm extends ModalNorm {

    /**
     * Averages of @URL{https://github.com/nytimes/covid-19-data/blob/master/mask-use/mask-use-by-county.csv}
     */
    public static double[] generalMaskAttitudes =
            {
                    0.079939529,
                    0.082918523,
                    0.12131795,
                    0.207724698,
                    0.508093571
            };

    /**
     * Assigned probabilities for the five labels
     */
    public static double[] generalMaskAttitudeProbabilities =
            {
                    0d, .25, .5, .75, 1
            };

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        activity.setMask(true);
        return activity;
    }

    /**
     * Wearing a mask applies to all activities that take place outside of the home environment
     */
    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

    /**
     * The person chooses voluntarily to wear a mask if they are working as a medical professional in all work-related
     * settings.
     *
     * In all other cases, the default reasoning for modal norms is followed.
     *
     * In a future version, statistics from a poll on mask wearing could potentially be used,
     * (see @URL{https://github.com/nytimes/covid-19-data/blob/master/mask-use/mask-use-by-county.csv})
     * However, this is hard to align with the government attitude which is used for the later obligation to wear masks
     */
    @Override
    public double calculateAttitude(AgentContextInterface<CandidateActivity> agentContextInterface, Activity activity) {
        if (agentContextInterface.getContext(Person.class).getDesignation().equals(Designation.medical) &&
                        activity.getActivityType().equals(ActivityType.WORK)) {
            return 0;
        }
        return super.calculateAttitude(agentContextInterface, activity);

        // TODO might use the statistics from https://github.com/nytimes/covid-19-data/blob/master/mask-use/mask-use-by-county.csv in future work
//        double p = agentContextInterface.getContext(BeliefContext.class).getRandom().nextDouble();
//        double lastP = 0;
//        for(int i = 0; i < ModalNorm.generalMaskAttitudes.length; i++) {
//            if(p > lastP && p < ModalNorm.generalMaskAttitudes[i])
//                return 1 - ModalNorm.generalMaskAttitudeProbabilities[i];
//        }
//        return 1;
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, int days) {
        return locationHistoryContext.getLastDaysFractionMask(days);
    }

    @Override
    double getFractionWithModeLastDays(LocationHistoryContext locationHistoryContext, long locationID, int days) {
        return locationHistoryContext.getLastDaysFractionMaskAt(days, locationID);
    }

    @Override
    public String toString() {
        return "AllowWearMask";
    }

    @Override
    public List<IFactor> getFactors() {
        return Arrays.asList(
                new FractionMask()
        );
    }
}
