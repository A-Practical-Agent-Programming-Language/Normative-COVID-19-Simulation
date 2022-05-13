package nl.uu.iss.ga.util.tracking.activities;

import nl.uu.iss.ga.model.data.Activity;
import nl.uu.iss.ga.model.data.dictionary.ActivityType;
import nl.uu.iss.ga.model.norm.Norm;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public interface InfluencedActivitiesInterface {
    int getDeltaCases();

    int getnInfected();

    void setNInfected(int nInfected);

    long getTimeStep();

    void activityCancelled(Activity activity, Norm norm);

    void activityContinuing(Activity activity);

    Map<Class<? extends Norm>, Map<ActivityType, Integer>> getActivitiesCancelledByNorm();

    Map<ActivityType, Integer> getCancelledActivities();

    Map<ActivityType, Integer> getContinuedActivities();

    Map<ActivityType, Integer> getTotalActivities();

    int getSumTotalActivities();

    Map<ActivityType, Double> getFractionActivitiesCancelled();

    default <T> Map<T, Integer> atomicIntMapToIntMap(Map<T, AtomicInteger> map) {
        Map<T, Integer> newMap = new HashMap<>();
        for(T key : map.keySet()) {
            newMap.put(key, map.get(key).intValue());
        }
        return newMap;
    }
}
