package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.util.Map;

public class LocationEntry {

    // Required for matching
    private final Long pid;
    private final int activity_number;

    // Redundant. May serve as check
    private final Long hid;
    private final ActivityType activity_type;
    private final ActivityTime starttime;
    private final int duration;
    private final TransportMode travelmode;

    // Actual location data
    private final Long lid;
    private final double longitude;
    private final double latitude;

    // Artificially added
    private Designation designation = Designation.none;
    private boolean isResidential = false;

    public LocationEntry(Long hid, Long pid, int activity_number, ActivityType activity_type, ActivityTime starttime, int duration, Long lid, double longitude, double latitude, TransportMode travelmode) {
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

    public Long getPid() {
        return pid;
    }

    public int getActivity_number() {
        return activity_number;
    }

    public Long getHid() {
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

    public Long getLocationID() {
        return lid;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public Designation getDesignation() {
        return designation;
    }

    public boolean isResidential() {
        return isResidential;
    }

    public void setDesignation(String designation, boolean isResidential) {
        this.designation = designation.isEmpty() ? Designation.none : Designation.valueOf(designation);
        this.isResidential = isResidential;
    }

    /**
     * Some norms, e.g. business closure, only apply to non-essential businesses or the like. From these norms,
     * both locations that are essential, and those that are residences can be excluded.
     *
     * @return True if this location can be excluded from a norm only applying to N.E.B. type businesses
     */
    public boolean isEssentialOrResidential() {
        return !this.designation.equals(Designation.none) || this.isResidential;
    }

    public void setResidential(boolean isResidential) {
        this.isResidential = isResidential;
    }

    public static LocationEntry fromLine(Map<String, String> keyValue) {
        return new LocationEntry(
                ParserUtil.parseAsLong(keyValue.get("hid")),
                ParserUtil.parseAsLong(keyValue.get("pid")),
                ParserUtil.parseAsInt(keyValue.get("activity_number")),
                CodeTypeInterface.parseAsEnum(ActivityType.class, keyValue.get("activity_type")),
                new ActivityTime(ParserUtil.parseAsInt(keyValue.get("start_time"))),
                ParserUtil.parseAsInt(keyValue.get("duration")),
                ParserUtil.parseAsLong(keyValue.get("lid")),
                ParserUtil.parseAsDouble(keyValue.get("longitude")),
                ParserUtil.parseAsDouble(keyValue.get("latitude")),
                CodeTypeInterface.parseAsEnum(TransportMode.class, keyValue.get("travel_mode"))
        );
    }
}
