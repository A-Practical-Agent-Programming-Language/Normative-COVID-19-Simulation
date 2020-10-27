package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.norm.Norm;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class InfluencedActivities {

    private final long tick;

    private final ConcurrentHashMap<Class<? extends Norm>, Map<ActivityType, AtomicInteger>> activitiesCancelledByNorm = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActivityType, AtomicInteger> cancelledActivities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActivityType, AtomicInteger> continuedActivities = new ConcurrentHashMap<>();

    public InfluencedActivities(long tick) {
        this.tick = tick;
    }

    public long getTick() {
        return tick;
    }

    public void activityCancelled(Activity activity, Norm norm) {
        if(!cancelledActivities.containsKey(activity.getActivityType()))
            cancelledActivities.put(activity.getActivityType(), new AtomicInteger());
        this.cancelledActivities.get(activity.getActivityType()).getAndIncrement();

        Class<? extends Norm> normHeader = norm.getClass();
        if(!this.activitiesCancelledByNorm.containsKey(normHeader))
            this.activitiesCancelledByNorm.put(normHeader, new HashMap<>());
        if(!this.activitiesCancelledByNorm.get(normHeader).containsKey(activity.getActivityType()))
            this.activitiesCancelledByNorm.get(normHeader).put(activity.getActivityType(), new AtomicInteger());
        this.activitiesCancelledByNorm.get(normHeader).get(activity.getActivityType()).getAndIncrement();
    }

    public void activityContinuing(Activity activity) {
        if(!this.continuedActivities.containsKey(activity.getActivityType()))
            this.continuedActivities.put(activity.getActivityType(), new AtomicInteger());
        this.continuedActivities.get(activity.getActivityType()).getAndIncrement();
    }

    public Map<Class<? extends Norm>, Map<ActivityType, Integer>> getActivitiesCancelledByNorm() {
        Map<Class<? extends Norm>, Map<ActivityType, Integer>> cancelled = new HashMap<>();
        for(Class<? extends Norm> norm : this.activitiesCancelledByNorm.keySet()) {
            cancelled.put(norm, atomicIntMapToIntMap(this.activitiesCancelledByNorm.get(norm)));
        }
        return cancelled;
    }

    public Map<ActivityType, Integer> getCancelledActivities() {
        return atomicIntMapToIntMap(this.cancelledActivities);
    }

    public Map<ActivityType, Integer> getContinuedActivities() {
        return atomicIntMapToIntMap(this.continuedActivities);
    }

    public Map<ActivityType, Double> getFractionActivitiesCancelled() {
        Map<ActivityType, Double> fractionCancelledTypes = new HashMap<>();

        Map<ActivityType, Integer> cancelledActivities = getCancelledActivities();
        Map<ActivityType, Integer> continuedActivities = getContinuedActivities();
        Set<ActivityType> activityTypes = new HashSet<>();
        activityTypes.addAll(cancelledActivities.keySet());
        activityTypes.addAll(continuedActivities.keySet());

        for(ActivityType activityType : activityTypes) {
            double cancelledPct = 0d;
            if(cancelledActivities.containsKey(activityType) && continuedActivities.containsKey(activityType)) {
                cancelledPct = (double) cancelledActivities.get(activityType) /
                        (cancelledActivities.get(activityType) + continuedActivities.get(activityType));
            } else if (cancelledActivities.containsKey(activityType)) {
                cancelledPct = 1d;
            }
            fractionCancelledTypes.put(activityType, cancelledPct);
        }
        return fractionCancelledTypes;
    }

    private <T> Map<T, Integer> atomicIntMapToIntMap(Map<T, AtomicInteger> map) {
        Map<T, Integer> newMap = new HashMap<>();
        for(T key : map.keySet()) {
            newMap.put(key, map.get(key).intValue());
        }
        return newMap;
    }
}
