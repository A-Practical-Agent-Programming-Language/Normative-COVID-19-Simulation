package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;

public class ActivityTime implements Comparable<ActivityTime>, Cloneable {

    public static final int SECONDS_IN_DAY = 60 * 60 * 24;
    public static final int SECONDS_IN_HOUR = 60 * 60;

    private int seconds;
    private int hour_of_day;
    private int minute_of_hour;
    private int seconds_of_minute;
    private String army_time_of_day;

    private DayOfWeek dayOfWeek;

    public ActivityTime(int secondsSinceSundayMidnight) {
        this.seconds = secondsSinceSundayMidnight;
        dayOfWeek = DayOfWeek.fromSecondsSinceSundayMidnight(secondsSinceSundayMidnight);
        this.hour_of_day = (this.seconds % SECONDS_IN_DAY) / SECONDS_IN_HOUR;
        this.minute_of_hour = (this.seconds % SECONDS_IN_HOUR) / 60;
        this.seconds_of_minute = (this.seconds % 60);
        this.army_time_of_day = String.format("%02d:%02d:%02d", this.hour_of_day, this.minute_of_hour, this.seconds_of_minute);
    }

    public int getSeconds() {
        return seconds;
    }

    public int getHour_of_day() {
        return hour_of_day;
    }

    public int getMinute_of_hour() {
        return minute_of_hour;
    }

    public int getSeconds_of_minute() {
        return seconds_of_minute;
    }

    public String getArmy_time_of_day() {
        return army_time_of_day;
    }

    public DayOfWeek getDayOfWeek() {
        return dayOfWeek;
    }

    @Override
    public String toString() {
        return this.dayOfWeek.toString() + " "  + this.army_time_of_day;
    }

    @Override
    public int compareTo(ActivityTime activityTime) {
        if(this.seconds == activityTime.seconds) return 0;
        else return this.seconds - activityTime.seconds;
    }

    @Override
    protected ActivityTime clone() {
        return new ActivityTime(this.seconds);
    }
}
