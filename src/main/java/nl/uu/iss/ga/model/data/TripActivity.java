package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.TransportMode;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.util.Map;

public class TripActivity extends Activity {
    private final TransportMode mode;
    private final boolean personWasDriver;
    private final boolean personWasPassenger;

    public TripActivity(
            int pid,
            int hid,
            ActivityType activityType,
            DetailedActivity detailed_activity,
            int start_time,
            int duration,
            int month,
            int day,
            int survey_id,
            TransportMode mode,
            boolean personWasDriver,
            boolean personWasPassenger
    ) {
        super(pid, hid, activityType, detailed_activity, start_time, duration, month, day, survey_id);
        this.mode = mode;
        this.personWasDriver = personWasDriver;
        this.personWasPassenger = personWasPassenger;
    }

    public TripActivity(Activity baseActivity, TransportMode mode, boolean personWasDriver, boolean personWasPassenger) {
        super(
                baseActivity.getPid(),
                baseActivity.getHid(),
                baseActivity.getActivityType(),
                baseActivity.getDetailed_activity(),
                baseActivity.getStart_time(),
                baseActivity.getDuration(),
                baseActivity.getMonth(),
                baseActivity.getDay(),
                baseActivity.getSurvey_id()
        );
        this.mode = mode;
        this.personWasDriver = personWasDriver;
        this.personWasPassenger = personWasPassenger;
    }


    public static TripActivity fromLine(Activity baseActivity, Map<String, String> keyValue) {
        return new TripActivity(
                baseActivity,
                CodeTypeInterface.parseAsEnum(TransportMode.class, keyValue.get("mode")),
                ParserUtil.parseIntAsBoolean(keyValue.get("driver_flag")),
                ParserUtil.parseIntAsBoolean(keyValue.get("passenger_flag"))
        );
    }
}
