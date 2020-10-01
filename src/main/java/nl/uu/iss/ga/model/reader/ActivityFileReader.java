package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.TripActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ActivityFileReader {

    private static final Logger LOGGER = Logger.getLogger(ActivityFileReader.class.getName());

    private final List<File> activityScheduleFiles;
    private final List<ActivitySchedule> activitySchedules;
    private final Map<Long, Map<Integer, LocationEntry>> locationMap;

    public ActivityFileReader(List<File> activityScheduleFiles, Map<Long, Map<Integer, LocationEntry>> locationMap) {
        this.activityScheduleFiles = activityScheduleFiles;
        this.locationMap = locationMap;

        this.activitySchedules = new ArrayList<>();
        for(File f : this.activityScheduleFiles) {
            this.activitySchedules.addAll(readActivities(f));
        }
    }

    public List<ActivitySchedule> getActivitySchedules() {
        return activitySchedules;
    }

    private List<ActivitySchedule> readActivities(File activityScheduleFile) {
        try(
                FileInputStream is = new FileInputStream(activityScheduleFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateActivities(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read activities file " + activityScheduleFile.toString(), e);
        }
        return Collections.emptyList();
    }

    private List<ActivitySchedule> iterateActivities(Scanner s) {
        String headers = s.nextLine();
        String[] headerIndices = headers.split(ParserUtil.SPLIT_CHAR);

        Map<Long, ActivitySchedule> schedules = new HashMap<>();
        SortedMap<ActivityTime, Activity> activities = new TreeMap<>();

        long currentPersonIndex = -1;
        while(s.hasNextLine()) {
            String line = s.nextLine();
            Map<String, String> keyValue = ParserUtil.zipLine(headerIndices, line);

            Activity activity = getActivityFromLine(keyValue);

            if(activity.getPid() != currentPersonIndex) {
                // Reset
                if(!activities.isEmpty()) {
                    schedules.put(currentPersonIndex, new ActivitySchedule(
                            activities.get(activities.lastKey()).getHid(),
                            currentPersonIndex,
                            activities)
                    );
                } else if (currentPersonIndex != -1) {
                    LOGGER.log(Level.WARNING, "Empty schedule file for PID " + currentPersonIndex);
                }
                activities = schedules.containsKey(activity.getPid()) ? schedules.get(activity.getPid()).getSchedule() : new TreeMap<>();
                currentPersonIndex = activity.getPid();
            }
            activities.put(activity.getStart_time(), activity);
        }

        if(!activities.isEmpty()) {
            schedules.put(currentPersonIndex, new ActivitySchedule(
                    activities.get(activities.lastKey()).getHid(),
                    currentPersonIndex,
                    activities
            ));
        }

        return new ArrayList<>(schedules.values());
    }

    public Map<Integer, ActivityType> failedDetailedActivities = new HashMap<>();

    private Activity getActivityFromLine(Map<String, String> keyValue) {
        Activity activity = Activity.fromLine(keyValue);
        if (activity.getActivityType().equals(ActivityType.TRIP)) {
            activity = TripActivity.fromLine(activity, keyValue);
        } else {
            if(this.locationMap.isEmpty()) {
                activity.setLocation(LocationEntry.fromLine(keyValue));
            } else {
                activity.setLocation(this.locationMap.get(activity.getPid()).get(activity.getActivityNumber()));
            }
        }
        if(activity.getDetailed_activity() == null && activity.getActivityType().equals(ActivityType.TRIP)) {
            activity.setDetailed_activity(DetailedActivity.TRIP);
        } else if (activity.getDetailed_activity() == null) {
            int detailedActivityNumber = ParserUtil.parseAsInt(keyValue.get("detailed_activity"));
            if (!this.failedDetailedActivities.containsKey(ParserUtil.parseAsInt(keyValue.get("detailed_activity")))) {
                this.failedDetailedActivities.put(detailedActivityNumber, activity.getActivityType());
            }
            activity.setDetailed_activity(DetailedActivity.NOT_IN_DICTIONARY);
        }

        return activity;
    }
}
