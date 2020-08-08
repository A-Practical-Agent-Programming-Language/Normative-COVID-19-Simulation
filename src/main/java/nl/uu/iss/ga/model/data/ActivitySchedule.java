package main.java.nl.uu.iss.ga.model.data;

import java.util.SortedMap;

public class ActivitySchedule {
    private final int household;
    private final int person;
    private final SortedMap<ActivityTime, Activity> schedule;

    public ActivitySchedule(int household, int person, SortedMap<ActivityTime, Activity> schedule) {
        this.household = household;
        this.person = person;
        this.schedule = schedule;
    }

    public int getHousehold() {
        return household;
    }

    public int getPerson() {
        return person;
    }

    public SortedMap<ActivityTime, Activity> getSchedule() {
        return schedule;
    }
}
