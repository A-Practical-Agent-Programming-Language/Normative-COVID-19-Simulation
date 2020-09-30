package main.java.nl.uu.iss.ga.simulation.agent.plan.activity;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentContextInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;

import java.util.logging.Level;

public class CancelActivityPlan extends RunOncePlan<CandidateActivity> {

    // Odds that the activity will be replaced by going home. Otherwise, this
    // activity will just be dropped, and the next activity will be scheduled in its place
    public static final double GO_HOME_IF_CANCELLED_PROBABILITY = .5;

    private CandidateActivity activity;

    public CancelActivityPlan(Activity activity) {
        this.activity = new CandidateActivity(activity);
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) {
        DayPlanContext context = planToAgentInterface.getContext(DayPlanContext.class);
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);

        if (!context.testIsDayOfWeek(beliefContext.getToday())) {
            context.resetDaySchedule(beliefContext.getToday());
        }

        CandidateActivity lastActivity = context.getLastActivity();
        ActivitySchedule schedule = planToAgentInterface.getContext(ActivitySchedule.class);
        Activity nextActivity = schedule.getScheduledActivityAfter(this.activity.getActivity().getStart_time().getSeconds());

        /* Stay home if one of:
            - Last activity was HOME
            - Next activity is HOME or WORK
            - This would have been the last activity of the day
            - Otherwise just go home in x% of the cases
         */

        if (
                nextActivity == null ||
                !nextActivity.getStart_time().getDayOfWeek()
                    .equals(this.activity.getActivity().getStart_time().getDayOfWeek()) ||
                this.activity.getActivity().getActivityType().equals(ActivityType.WORK) ||
                this.activity.getActivity().getActivityType().equals(ActivityType.HOME) ||
                (lastActivity != null && lastActivity.getActivity().getActivityType().equals(ActivityType.HOME)) ||
                beliefContext.getRandom().nextDouble() < GO_HOME_IF_CANCELLED_PROBABILITY
        ) {
            this.activity.makeHome(new AgentContextInterface<>(planToAgentInterface.getAgent()));

            int starttime = context.getFirstAvailableTime();
            ActivityTime scheduledTime = this.activity.getActivity().getStart_time();

            if (
                    lastActivity != null &&
                    !lastActivity.getActivity().getActivityType().equals(ActivityType.HOME) &&
                    context.getLastTripActivity() != null
            ) {
                starttime += context.getLastTripActivity().getDuration();
                context.setLastTripActivity(null);
            }

            this.activity.getActivity().setStart_time(
                    new ActivityTime(starttime));

            if (nextActivity != null && !nextActivity.getStart_time().getDayOfWeek()
                    .equals(activity.getActivity().getStart_time().getDayOfWeek()))
            {
                activity.getActivity().setDuration(activity.getActivity().getStart_time().getDurationUntilEndOfDay());
            }

            // Sanity check
            if(
                    !activity.getActivity().getStart_time().getDayOfWeek().equals(scheduledTime.getDayOfWeek())
            ) {
                ActivityTime ends = new ActivityTime(activity.getActivity().getStart_time().getSeconds() + activity.getActivity().getDuration());
                if(!(ends.getDayOfWeek() == null && scheduledTime.getDayOfWeek().equals(DayOfWeek.SUNDAY)) &&
                        (ends.getDayOfWeek() != null && !ends.getDayOfWeek().equals(scheduledTime.getDayOfWeek()))) {
                    Platform.getLogger().log(getClass(), Level.WARNING, String.format(
                            "When modifying activity %d of person %d to cancel the activity, the day was changed from %s to %s",
                            activity.getActivity().getActivityNumber(),
                            activity.getActivity().getPid(),
                            scheduledTime.getDayOfWeek(),
                            ends.getDayOfWeek()
                    ));
                }
            }

            context.addCandidateActivity(activity);
            return this.activity;
        } else {
            // This means the agent does not perform any activity. The next activity can take the time
            // of the current activity that is cancelled
            context.setAdjustTime(true);
            return null;
        }
    }
}
