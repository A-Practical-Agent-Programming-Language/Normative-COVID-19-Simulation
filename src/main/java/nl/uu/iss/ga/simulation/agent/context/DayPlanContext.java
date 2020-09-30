package main.java.nl.uu.iss.ga.simulation.agent.context;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.TripActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;

public class DayPlanContext implements Context {

    private DayOfWeek dayOfWeek;
    private List<CandidateActivity> daySchedule;
    private boolean adjustTime = false;

    private TripActivity lastTripActivity;

    public boolean testIsDayOfWeek(DayOfWeek dayOfWeek) {
        return this.dayOfWeek != null && this.dayOfWeek.equals(dayOfWeek);
    }

    public void resetDaySchedule(DayOfWeek dayOfWeek) {
        this.dayOfWeek = dayOfWeek;
        this.daySchedule = new ArrayList<>();
        this.lastTripActivity = null;
        this.adjustTime = false;
    }

    public void addCandidateActivity(CandidateActivity activity) {
        if(!activity.getActivity().getStart_time().getDayOfWeek().equals(this.dayOfWeek)) {
            Platform.getLogger().log(getClass(), Level.WARNING, String.format(
                    "Trying to add activity for %s but today is %s",
                    activity.getActivity().getStart_time().getDayOfWeek(), this.dayOfWeek));
        }
        for(int i = 0; i < daySchedule.size(); i++) {
            if(activity.getActivity().getStart_time().getSeconds() < this.daySchedule.get(i).getActivity().getStart_time().getSeconds()) {
                this.daySchedule.add(i, activity);
                return;
            }
        }
        this.daySchedule.add(activity);
    }

    public CandidateActivity getLastActivity() {
        if(this.daySchedule.isEmpty()) {
            return null;
        } else {
            return this.daySchedule.get(this.daySchedule.size() - 1);
        }
    }

    public int getFirstAvailableTime() {
        if(this.daySchedule.isEmpty()) {
            return this.dayOfWeek.getSecondsSinceMidnightForDayStart();
        } else {
            return getLastActivity().getActivity().getStart_time().getSeconds() +
                    getLastActivity().getActivity().getDuration();
        }
    }

    public TripActivity getLastTripActivity() {
        return lastTripActivity;
    }

    public void setLastTripActivity(TripActivity lastTripActivity) {
        this.lastTripActivity = lastTripActivity;
    }

    public boolean isAdjustTime() {
        return adjustTime;
    }

    public void setAdjustTime(boolean adjustTime) {
        this.adjustTime = adjustTime;
    }
}
