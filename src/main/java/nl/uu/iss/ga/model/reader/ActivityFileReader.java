package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.Person;
import main.java.nl.uu.iss.ga.model.data.TripActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class ActivityFileReader {

    public static final String VA_ACTIVITY_HEADERS =
            "hid,pid,activity_number,activity_type,detailed_activity,start_time,duration,mode,driver_flag,passenger_flag,month,day,survey_id";
    public static final String[] VA_ACTIVITY_HEADER_INDICES = VA_ACTIVITY_HEADERS.split(ParserUtil.SPLIT_CHAR);

    private final File activityScheduleFile;
    private final Map<Integer, Person> personMap;
    private final List<ActivitySchedule> activitySchedules;

    public ActivityFileReader(File activityScheduleFile, Map<Integer, Person> personMap) {
        this.activityScheduleFile = activityScheduleFile;
        this.personMap = personMap;
        this.activitySchedules = readActivities();
    }

    public List<ActivitySchedule> getActivitySchedules() {
        return activitySchedules;
    }

    private List<ActivitySchedule> readActivities() {
        try(
                FileInputStream is = new FileInputStream(this.activityScheduleFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateActivities(s);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Collections.emptyList();
    }

    private List<ActivitySchedule> iterateActivities(Scanner s) {
        Map<Integer, Person> householdMap = new TreeMap<>();
        s.nextLine(); // Skip headers

        List<ActivitySchedule> schedules = new LinkedList<>();
        SortedMap<Integer, Activity> activities = new TreeMap<>();

        int currentPersonIndex = -1;
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

        return schedules;
    }

    private Activity getActivityFromLine(Map<String, String> keyValue) {
        Activity activity = Activity.fromLine(keyValue);
        if (activity.getActivityType().equals(ActivityType.TRIP)) {
            activity = TripActivity.fromLine(activity, keyValue);
        }
        return activity;
    }
}
