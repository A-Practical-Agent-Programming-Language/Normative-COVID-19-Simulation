package nl.uu.iss.ga.util.tracking.activities;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.norm.Norm;

import java.util.Map;

public class SuppressCalculationsActivityTracker implements InfluencedActivitiesInterface {
    @Override
    public int getDeltaCases() {
        return 0;
    }

    @Override
    public int getnInfected() {
        return 0;
    }

    @Override
    public void setNInfected(int nInfected) {

    }

    @Override
    public long getTimeStep() {
        return 0;
    }

    @Override
    public void activityCancelled(Activity activity, Norm norm) {

    }

    @Override
    public void activityContinuing(Activity activity) {

    }

    @Override
    public Map<Class<? extends Norm>, Map<ActivityType, Integer>> getActivitiesCancelledByNorm() {
        return null;
    }

    @Override
    public Map<ActivityType, Integer> getCancelledActivities() {
        return null;
    }

    @Override
    public Map<ActivityType, Integer> getContinuedActivities() {
        return null;
    }

    @Override
    public Map<ActivityType, Integer> getTotalActivities() {
        return null;
    }

    @Override
    public int getSumTotalActivities() {
        return 0;
    }

    @Override
    public Map<ActivityType, Double> getFractionActivitiesCancelled() {
        return null;
    }
}
