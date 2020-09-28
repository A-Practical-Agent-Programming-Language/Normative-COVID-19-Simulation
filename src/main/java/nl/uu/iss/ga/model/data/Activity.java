package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

import java.util.Map;

public class Activity extends Goal implements Cloneable {

    private final long pid;
    private final long hid;
    private final int activityNumber;
    private ActivityType activityType;
    private DetailedActivity detailed_activity;
    private ActivityTime start_time;
    private int duration;

    private final int month;
    private final int day;
    private final int survey_id;

    private LocationEntry location;

    public Activity(long pid, long hid, int activityNumber, ActivityType activityType, DetailedActivity detailed_activity, ActivityTime start_time, int duration, int month, int day, int survey_id) {
        this.pid = pid;
        this.hid = hid;
        this.activityNumber = activityNumber;
        this.activityType = activityType;
        this.detailed_activity = detailed_activity;
        this.start_time = start_time;
        this.duration = duration;
        this.month = month;
        this.day = day;
        this.survey_id = survey_id;
    }

    public long getPid() {
        return pid;
    }

    public long getHid() {
        return hid;
    }

    public int getActivityNumber() {
        return activityNumber;
    }

    public ActivityType getActivityType() {
        return activityType;
    }

    public void setActivityType(ActivityType activityType) {
        this.activityType = activityType;
    }

    public DetailedActivity getDetailed_activity() {
        return detailed_activity;
    }

    public void setDetailed_activity(DetailedActivity detailed_activity) {
        this.detailed_activity = detailed_activity;
    }

    public ActivityTime getStart_time() {
        return start_time;
    }

    public void setStart_time(ActivityTime start_time) {
        this.start_time = start_time;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public int getMonth() {
        return month;
    }

    public int getDay() {
        return day;
    }

    public int getSurvey_id() {
        return survey_id;
    }

    public LocationEntry getLocation() {
        return location;
    }

    public void setLocation(LocationEntry location) {
        this.location = location;
    }

    public static Activity fromLine(Map<String, String> keyValue) {
        return new Activity(
                ParserUtil.parseAsLong(keyValue.get("pid")),
                ParserUtil.parseAsLong(keyValue.get("hid")),
                ParserUtil.parseAsInt(keyValue.get("activity_number")),
                CodeTypeInterface.parseAsEnum(ActivityType.class, keyValue.get("activity_type")),
                CodeTypeInterface.parseAsEnum(DetailedActivity.class, keyValue.get("detailed_activity")),
                new ActivityTime(ParserUtil.parseAsInt(keyValue.get("start_time"))),
                ParserUtil.parseAsInt(keyValue.get("duration")),
                ParserUtil.parseAsInt(keyValue.get("month")),
                ParserUtil.parseAsInt(keyValue.get("day")),
                ParserUtil.parseAsInt(keyValue.get("survey_id"))
        );
    }

    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        // Activity should never be associated with a plan as a goal, and should never be achieved
        return false;
    }

    @Override
    public String toString() {
        // start_time, duration, location, mask_state, disease_state
        if(this.location == null) {
            System.out.println("Got null location for some reason");
        }
        return String.format(
                "%s (%s) %s - %s",
                this.getActivityType(),
                this.getDetailed_activity(),
                this.start_time,
                new ActivityTime(this.start_time.getSeconds() + this.duration)
        );
    }

    @Override
    protected Activity clone() {
        Activity activity = new Activity(
                this.pid,
                this.hid,
                this.activityNumber,
                this.activityType,
                this.detailed_activity,
                this.start_time.clone(),
                this.duration,
                this.month,
                this.day,
                this.survey_id
        );
        activity.setLocation(this.location);
        return activity;
    }
}
