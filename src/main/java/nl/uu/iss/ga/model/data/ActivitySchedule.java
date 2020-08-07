package main.java.nl.uu.iss.ga.model.data;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.SortedMap;

public class ActivitySchedule {
    private final int household;
    private final int person;
    private final SortedMap<Integer, Activity> schedule;

    public ActivitySchedule(int household, int person, SortedMap<Integer, Activity> schedule) {
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

    public SortedMap<Integer, Activity> getSchedule() {
        return schedule;
    }
}
