package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.util.Map;

public class LocationEntry {

    private static final String LOCATION_HEADERS = "hid,pid,activity_number,activity_type,start_time,duration,lid,longitude,latitude,travel_mode";
    private static final String[] LOCATION_HEADER_INDICES = LOCATION_HEADERS.split(ParserUtil.SPLIT_CHAR);

    // Required for matching
    private final int pid;
    private final int activity_number;

    // Redundant. May serve as check
    private final int hid;
    private final ActivityType activity_type;
    private final ActivityTime starttime;
    private final int duration;
    private final TransportMode travelmode;

    // Actual location data
    private final int lid;
    private final double longitude;
    private final double latitude;

    public LocationEntry(int hid, int pid, int activity_number, ActivityType activity_type, ActivityTime starttime, int duration, int lid, double longitude, double latitude, TransportMode travelmode) {
        this.pid = pid;
        this.activity_number = activity_number;
        this.hid = hid;
        this.activity_type = activity_type;
        this.starttime = starttime;
        this.duration = duration;
        this.travelmode = travelmode;
        this.lid = lid;
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public int getPid() {
        return pid;
    }

    public int getActivity_number() {
        return activity_number;
    }

    public int getHid() {
        return hid;
    }

    public ActivityType getActivity_type() {
        return activity_type;
    }

    public ActivityTime getStarttime() {
        return starttime;
    }

    public int getDuration() {
        return duration;
    }

    public TransportMode getTravelmode() {
        return travelmode;
    }

    public int getLocationID() {
        return lid;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public static LocationEntry fromLine(String line) {
        Map<String, String> keyValue = ParserUtil.zipLine(LOCATION_HEADER_INDICES, line);
        return new LocationEntry(
                ParserUtil.parseAsInt(keyValue.get("hid")),
                ParserUtil.parseAsInt(keyValue.get("pid")),
                ParserUtil.parseAsInt(keyValue.get("activity_number")),
                CodeTypeInterface.parseAsEnum(ActivityType.class, keyValue.get("activity_type")),
                new ActivityTime(ParserUtil.parseAsInt(keyValue.get("start_time"))),
                ParserUtil.parseAsInt(keyValue.get("duration")),
                ParserUtil.parseAsInt(keyValue.get("lid")),
                ParserUtil.parseAsDouble(keyValue.get("longitude")),
                ParserUtil.parseAsDouble(keyValue.get("latitude")),
                CodeTypeInterface.parseAsEnum(TransportMode.class, keyValue.get("travel_mode"))
        );
    }
}
