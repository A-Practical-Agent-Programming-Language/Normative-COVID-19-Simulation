package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.Simulation;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Goal;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Activity extends Goal implements Cloneable {
    private static final Logger LOGGER = Logger.getLogger(Simulation.class.getName());

    private final long pid;
    private final long hid;
    private final int activityNumber;
    private ActivityType activityType;
    private DetailedActivity detailed_activity;
    private ActivityTime start_time;
    private int duration;

    private LocationEntry location;

    public Activity(long pid, long hid, int activityNumber, ActivityType activityType, DetailedActivity detailed_activity, ActivityTime start_time, int duration) {
        this.pid = pid;
        this.hid = hid;
        this.activityNumber = activityNumber;
        this.activityType = activityType;
        this.detailed_activity = detailed_activity;
        this.start_time = start_time;
        this.duration = duration;
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

    public LocationEntry getLocation() {
        return location;
    }

    public void setLocation(LocationEntry location) {
        this.location = location;
    }

    public static Activity fromLine(Map<String, String> keyValue) {
        DetailedActivity detailedActivity = keyValue.containsKey("detailed_activity") ?
                CodeTypeInterface.parseAsEnum(DetailedActivity.class, keyValue.get("detailed_activity")) :
                DetailedActivity.NOT_SPECIFIED;

        return new Activity(
                ParserUtil.parseAsLong(keyValue.get("pid")),
                ParserUtil.parseAsLong(keyValue.get("hid")),
                ParserUtil.parseAsInt(keyValue.get("activity_number")),
                CodeTypeInterface.parseAsEnum(ActivityType.class, keyValue.get("activity_type")),
                detailedActivity,
                new ActivityTime(ParserUtil.parseAsInt(keyValue.get("start_time"))),
                ParserUtil.parseAsInt(keyValue.get("duration"))
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
            LOGGER.log(Level.WARNING,
                    String.format("Got null location for activity %d of person %d", this.getActivityNumber(), this.getPid()));
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
                this.duration
        );
        activity.setLocation(this.location);
        return activity;
    }
}
