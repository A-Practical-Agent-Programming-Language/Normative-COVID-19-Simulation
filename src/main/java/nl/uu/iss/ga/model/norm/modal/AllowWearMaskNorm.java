package main.java.nl.uu.iss.ga.model.norm.modal;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.Designation;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;

public class AllowWearMaskNorm extends ModalNorm {

    @Override
    public CandidateActivity transformActivity(CandidateActivity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        activity.setMask(true);
        return activity;
    }

    @Override
    public boolean applicable(Activity activity, AgentContextInterface<CandidateActivity> agentContextInterface) {
        return !activity.getActivityType().equals(ActivityType.HOME);
    }

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
}
