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

    private final ConcurrentHashMap<Class<? extends Norm>, ConcurrentHashMap<ActivityType, AtomicInteger>> activitiesCancelledByNorm = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActivityType, AtomicInteger> cancelledActivities = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<ActivityType, AtomicInteger> continuedActivities = new ConcurrentHashMap<>();

    public InfluencedActivities(long tick, Set<Class<? extends Norm>> norms) {
        this.tick = tick;
        for(Class<? extends Norm> n : norms) {
            ConcurrentHashMap<ActivityType, AtomicInteger> map = new ConcurrentHashMap<>();
            for(ActivityType type : ActivityType.values()) {
                map.put(type, new AtomicInteger());
            }
            this.activitiesCancelledByNorm.put(n, map);
        }

        for(ActivityType type : ActivityType.values()) {
            cancelledActivities.put(type, new AtomicInteger());
            continuedActivities.put(type, new AtomicInteger());
        }

    }

    public long getTick() {
        return tick;
    }

    public void activityCancelled(Activity activity, Norm norm) {
        this.cancelledActivities.get(activity.getActivityType()).getAndIncrement();
        this.activitiesCancelledByNorm.get(norm.getClass()).get(activity.getActivityType()).getAndIncrement();
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

    public Map<ActivityType, Integer> getTotalActivities() {
        Map<ActivityType, Integer> continuing = getContinuedActivities();
        Map<ActivityType, Integer> cancelled = getCancelledActivities();
        for(ActivityType type : cancelled.keySet()) {
            continuing.put(type, continuing.getOrDefault(type, 0) + cancelled.get(type));
        }
        return continuing;
    }

    public int getSumTotalActivities() {
        return getTotalActivities().values().stream().reduce(Integer::sum).orElse(0);
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
