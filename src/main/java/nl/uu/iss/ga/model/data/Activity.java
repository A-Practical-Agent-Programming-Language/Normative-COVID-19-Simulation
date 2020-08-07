package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

import java.util.Map;

public class Activity extends Goal {

    private final int pid;
    private final int hid;
    private final ActivityType activityType;
    private final DetailedActivity detailed_activity;
    private final int start_time;
    private final int duration;

    private final int month;
    private final int day;
    private final int survey_id;

    public Activity(int pid, int hid, ActivityType activityType, DetailedActivity detailed_activity, int start_time, int duration, int month, int day, int survey_id) {
        this.pid = pid;
        this.hid = hid;
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

    public ActivityType getActivityType() {
        return activityType;
    }

    public DetailedActivity getDetailed_activity() {
        return detailed_activity;
    }

    public int getStart_time() {
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

    public static Activity fromLine(Map<String, String> keyValue) {
        return new Activity(
                ParserUtil.parseAsInt(keyValue.get("pid")),
                ParserUtil.parseAsInt(keyValue.get("hid")),
                CodeTypeInterface.parseAsEnum(ActivityType.class, keyValue.get("activity_type")),
                CodeTypeInterface.parseAsEnum(DetailedActivity.class, keyValue.get("detailed_activity")),
                ParserUtil.parseAsInt(keyValue.get("start_time")),
                ParserUtil.parseAsInt(keyValue.get("duration")),
                ParserUtil.parseAsInt(keyValue.get("month")),
                ParserUtil.parseAsInt(keyValue.get("day")),
                ParserUtil.parseAsInt(keyValue.get("survey_id"))
        );
    }

    @Override
    public boolean isAchieved(AgentContextInterface agentContextInterface) {
        // When is an activity achieved? Presumably never? If it is achieved,
        // should a similar new goal be adopted for the week after?
        return false;
    }
}
