package nl.uu.iss.ga.model.data;

import nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.LinkedList;
import java.util.List;
import java.util.SortedMap;

public class ActivitySchedule implements Context {
    private final long household;
    private final long person;
    private final SortedMap<ActivityTime, Activity> schedule;

    public ActivitySchedule(long household, long person, SortedMap<ActivityTime, Activity> schedule) {
        this.household = household;
        this.person = person;
        this.schedule = schedule;
    }

    public long getHousehold() {
        return household;
    }

    public long getPerson() {
        return person;
    }

    public SortedMap<ActivityTime, Activity> getSchedule() {
        return schedule;
    }

    /**
     * Return the first activity that is not a trip that is planned after the given time
     **/
    public Activity getScheduledActivityAfter(DayOfWeek dayOfWeek, int secondsOfDay) {
        return getScheduledActivityAfter(dayOfWeek.getSecondsSinceMidnightForDayStart() + secondsOfDay);
    }

    /**
     * Return the first activity that is not a trip that is planned after the given time
     */
    public Activity getScheduledActivityAfter(int secondsSinceSundayMidnight) {
        for(ActivityTime time : this.schedule.keySet()) {
            if(time.getSeconds() > secondsSinceSundayMidnight && !(this.schedule.get(time) instanceof TripActivity)) {
                return this.schedule.get(time);
            }
        }
        return null;
    }

    public void splitActivitiesByDay() {
        List<Activity> insertActivities = new LinkedList<>();
        for(ActivityTime activityTime : this.schedule.keySet()) {
            // -1 to avoid going to the next day if activity ends at 23:59:59
            ActivityTime endTime = new ActivityTime(activityTime.getSeconds() + this.schedule.get(activityTime).getDuration() - 1);
            if(!activityTime.getDayOfWeek().equals(endTime.getDayOfWeek())) {
                this.schedule.get(activityTime).setDuration(activityTime.getDurationUntilEndOfDay());
                Activity addedMorningActivity = this.schedule.get(activityTime).clone();
                addedMorningActivity.setStart_time(new ActivityTime(endTime.getDayOfWeek().getSecondsSinceMidnightForDayStart()));
                addedMorningActivity.setDuration(endTime.getSeconds() - addedMorningActivity.getStart_time().getSeconds());
                insertActivities.add(addedMorningActivity);
            }
        }

        for(Activity activity : insertActivities) {
            this.schedule.put(activity.getStart_time(), activity);
        }
    }
}
