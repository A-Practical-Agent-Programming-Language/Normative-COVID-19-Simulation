package main.java.nl.uu.iss.ga.simulation.agent.plan.activity;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

import java.util.logging.Level;
import java.util.logging.Logger;

public class ExecuteScheduledActivityPlan extends RunOncePlan<CandidateActivity> {
    private static final Logger LOGGER = Logger.getLogger(ExecuteScheduledActivityPlan.class.getName());

    private CandidateActivity activity;

    public ExecuteScheduledActivityPlan(CandidateActivity activity) {
        this.activity = activity;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        DayPlanContext context = planToAgentInterface.getContext(DayPlanContext.class);
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);
        ActivitySchedule schedule = planToAgentInterface.getContext(ActivitySchedule.class);

        if (!context.testIsDayOfWeek(beliefContext.getToday())) {
            context.resetDaySchedule(beliefContext.getToday());
        }

        if (context.isAdjustTime()) {
            adjustTime(context, schedule);
        }

        performSanityCheck(beliefContext.getToday());

        context.addCandidateActivity(activity);

        return activity;
    }

    private void adjustTime(DayPlanContext context, ActivitySchedule schedule) {
        int newStartTime = context.getFirstAvailableTime();

        if (context.getLastTripActivity() != null) {
            newStartTime += context.getLastTripActivity().getDuration();
            context.setLastTripActivity(null);
        }

        ActivityTime makeStart = new ActivityTime(newStartTime);
        if(!makeStart.getDayOfWeek().equals(activity.getActivity().getStart_time().getDayOfWeek())) {
            LOGGER.log(Level.INFO, String.format(
                    "Activity was changed from %s to %s",
                    activity.getActivity().getStart_time().getDayOfWeek(),
                    makeStart.getDayOfWeek()));
        }
        activity.getActivity().setStart_time(makeStart);
        Activity nextPlannedActivity = schedule.getScheduledActivityAfter(activity.getActivity().getStart_time().getSeconds());

        if (activity.getActivity().getActivityType().equals(ActivityType.HOME) &&
                (
                        nextPlannedActivity == null ||
                                !nextPlannedActivity.getStart_time().getDayOfWeek()
                                        .equals(activity.getActivity().getStart_time().getDayOfWeek())
                )
        ) {
            // Make duration end of day
            activity.getActivity().setDuration(activity.getActivity().getStart_time().getDurationUntilEndOfDay());
        } else if (nextPlannedActivity.getActivityType().equals(ActivityType.WORK)) {
            // Stretch current activity, because work won't start earlier.
            activity.getActivity().setDuration(nextPlannedActivity.getStart_time().getSeconds() -
                    activity.getActivity().getStart_time().getSeconds());
        }
    }

    private void performSanityCheck(DayOfWeek today) {
        // Sanity check
        if (
                !activity.getActivity().getStart_time().getDayOfWeek().equals(today) ||
                        !new ActivityTime(activity.getActivity().getStart_time().getSeconds() + activity.getActivity().getDuration() -1 ).getDayOfWeek().equals(today)
        ) {
            LOGGER.log(Level.WARNING, String.format(
                    "Day changed when shifting activity %d for person %d",
                    activity.getActivity().getActivityNumber(), activity.getActivity().getPid()));
        }
    }
}
