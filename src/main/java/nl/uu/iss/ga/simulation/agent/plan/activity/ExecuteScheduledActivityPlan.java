package main.java.nl.uu.iss.ga.simulation.agent.plan.activity;

import main.java.nl.uu.iss.ga.model.data.Activity;
import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.ActivityType;
import main.java.nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import main.java.nl.uu.iss.ga.simulation.agent.context.DayPlanContext;
import nl.uu.cs.iss.ga.sim2apl.core.agent.PlanToAgentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.plan.PlanExecutionError;
import nl.uu.cs.iss.ga.sim2apl.core.plan.builtin.RunOncePlan;

public class ExecuteScheduledActivityPlan extends RunOncePlan<CandidateActivity> {

    private CandidateActivity activity;

    public ExecuteScheduledActivityPlan(CandidateActivity activity) {
        this.activity = activity;
    }

    @Override
    public CandidateActivity executeOnce(PlanToAgentInterface<CandidateActivity> planToAgentInterface) throws PlanExecutionError {
        DayPlanContext context = planToAgentInterface.getContext(DayPlanContext.class);
        BeliefContext beliefContext = planToAgentInterface.getContext(BeliefContext.class);
        ActivitySchedule schedule = planToAgentInterface.getContext(ActivitySchedule.class);

        // TODO this also needs to be applied in GoHomePlan
        if (!context.testIsDayOfWeek(beliefContext.getEnvironmentInterface().getToday())) {
            context.resetDaySchedule(beliefContext.getEnvironmentInterface().getToday());
        }

        if (context.isAdjustTime()) {
            int newStartTime = context.getFirstAvailableTime();

            if (context.getLastTripActivity() != null) {
                newStartTime += context.getLastTripActivity().getDuration();
                context.setLastTripActivity(null);
            }

            ActivityTime makeStart = new ActivityTime(newStartTime);
            if(!makeStart.getDayOfWeek().equals(activity.getActivity().getStart_time().getDayOfWeek())) {
                System.out.printf("Activity was changed from %s to %s%n", activity.getActivity().getStart_time().getDayOfWeek(), makeStart.getDayOfWeek());
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

        // Sanity check
        if (
                !activity.getActivity().getStart_time().getDayOfWeek().equals(beliefContext.getEnvironmentInterface().getToday()) ||
                        !new ActivityTime(activity.getActivity().getStart_time().getSeconds() + activity.getActivity().getDuration() -1 ).getDayOfWeek().equals(beliefContext.getEnvironmentInterface().getToday())
        ) {
            System.err.println("Scheduled: Day changed when shifting time?");
        }


        context.addCandidateActivity(activity);

        return activity;
    }
}
