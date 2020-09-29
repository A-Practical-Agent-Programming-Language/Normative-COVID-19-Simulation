package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.*;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DetailedActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class ActivityFileReader {

    public static final String VA_ACTIVITY_HEADERS =
            "hid,pid,activity_number,activity_type,detailed_activity,start_time,duration,mode,driver_flag,passenger_flag,month,day,survey_id";
    public static final String[] VA_ACTIVITY_HEADER_INDICES = VA_ACTIVITY_HEADERS.split(ParserUtil.SPLIT_CHAR);

    private final List<File> activityScheduleFiles;
    private final Map<Long, Person> personMap;
    private final List<ActivitySchedule> activitySchedules;
    private final Map<Long, Map<Integer, LocationEntry>> locationMap;

    public ActivityFileReader(List<File> activityScheduleFiles, Map<Long, Person> personMap, Map<Long, Map<Integer, LocationEntry>> locationMap) {
        this.activityScheduleFiles = activityScheduleFiles;
        this.personMap = personMap;
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
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<ActivitySchedule> iterateActivities(Scanner s) {
        Map<Integer, Person> householdMap = new TreeMap<>();
        s.nextLine(); // Skip headers

        List<ActivitySchedule> schedules = new LinkedList<>();
        SortedMap<ActivityTime, Activity> activities = new TreeMap<>();

        long currentPersonIndex = -1;
        while(s.hasNextLine()) {
            String line = s.nextLine();
            Map<String, String> keyValue = ParserUtil.zipLine(VA_ACTIVITY_HEADER_INDICES, line);

            Activity activity = getActivityFromLine(keyValue);

            if(activity.getPid() != currentPersonIndex) {
                // Reset
                if(!activities.isEmpty()) {
                    schedules.add(new ActivitySchedule(
                            activities.get(activities.lastKey()).getHid(),
                            currentPersonIndex,
                            activities)
                    );
                }
                activities = new TreeMap<>();
                currentPersonIndex = activity.getPid();
            }
            activities.put(activity.getStart_time(), activity);
        }

        if(!activities.isEmpty()) {
            schedules.add(new ActivitySchedule(
                    activities.get(activities.lastKey()).getHid(),
                    currentPersonIndex,
                    activities
            ));
        }

        return schedules;
    }

    public Map<Integer, ActivityType> failedDetailedActivities = new HashMap<>();

    private Activity getActivityFromLine(Map<String, String> keyValue) {
        Activity activity = Activity.fromLine(keyValue);
        if (activity.getActivityType().equals(ActivityType.TRIP)) {
            activity = TripActivity.fromLine(activity, keyValue);
        } else {
            activity.setLocation(this.locationMap.get(activity.getPid()).get(activity.getActivityNumber()));
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
