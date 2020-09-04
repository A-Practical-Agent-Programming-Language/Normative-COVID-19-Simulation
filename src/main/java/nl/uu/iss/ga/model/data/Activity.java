package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

import java.util.Map;

public class Activity extends Goal {

    private final int pid;
    private final int hid;
    private final int activityNumber;
    private final ActivityType activityType;
    private DetailedActivity detailed_activity;
    private final ActivityTime start_time;
    private final int duration;

    private final int month;
    private final int day;
    private final int survey_id;

    private LocationEntry location;

    public Activity(int pid, int hid, int activityNumber, ActivityType activityType, DetailedActivity detailed_activity, ActivityTime start_time, int duration, int month, int day, int survey_id) {
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

    public int getPid() {
        return pid;
    }

    public int getHid() {
        return hid;
    }

    public int getActivityNumber() {
        return activityNumber;
    }

    public ActivityType getActivityType() {
        return activityType;
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

    public int getDuration() {
        return duration;
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
                ParserUtil.parseAsInt(keyValue.get("pid")),
                ParserUtil.parseAsInt(keyValue.get("hid")),
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
        return String.format(
                "%s;%d;%d;%s;%s",
                this.start_time.getArmy_time_of_day(),
                this.duration,
                this.location.getLocationID(),
                RiskMitigationPolicy.NONE,
                DiseaseState.SUSCEPTIBLE
        );
    }
}
