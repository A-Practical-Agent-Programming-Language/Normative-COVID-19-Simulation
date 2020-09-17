package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Deprecated // I guess this is not a required class with the new setup
public abstract class RegimentedNorm extends Norm{

    private List<ActivityType> affectedActivities;
    private List<DetailedActivity> affectedDetailedActivities;

    public RegimentedNorm(String name, ActivityType... affectedActivities) {
        super(name, NORM_TYPE.PROHIBITION, true);
        this.affectedActivities = Arrays.asList(affectedActivities);
    }

    public RegimentedNorm(String name, DetailedActivity... affectedActivities) {
        super(name, NORM_TYPE.PROHIBITION, true);
        this.affectedDetailedActivities = Arrays.asList(affectedActivities);
    }

    public List<ActivityType> getAffectedActivities() {
        return new ArrayList<>(affectedActivities);
    }

    public List<DetailedActivity> getAffectedDetailedActivities() {
        return new ArrayList<>(affectedDetailedActivities);
    }

    public boolean activityAffected(ActivityType activityType) {
        return this.affectedActivities.contains(activityType);
    }

    public boolean activityAffected(DetailedActivity activityType) {
        boolean affected = false;
        affected |= this.affectedDetailedActivities.contains(activityType);
        // TODO check if detailed activity belongs to any of the activities in this.affectedActivities
        return affected;
    }
}
