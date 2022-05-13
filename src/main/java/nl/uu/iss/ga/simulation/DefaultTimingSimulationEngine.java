package nl.uu.iss.ga.simulation;


import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.step.AbstractSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.step.EnvironmentInterface;
import nl.uu.cs.iss.ga.sim2apl.core.step.StepExecutor;
import nl.uu.iss.ga.util.config.SimulationArguments;
import nl.uu.iss.ga.util.tracking.ScheduleTrackerGroup;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public class DefaultTimingSimulationEngine<T> extends AbstractSimulationEngine<T> {
    private final StepExecutor<T> executor;
    private final ScheduleTrackerGroup timingsTracker;
    private final SimulationArguments arguments;

    public DefaultTimingSimulationEngine(Platform platform, SimulationArguments arguments, int nIterations, EnvironmentInterface<T>... hookProcessors) {

        super(platform, nIterations, hookProcessors);
        this.executor = platform.getStepExecutor();
        this.arguments = arguments;

        this.timingsTracker = new ScheduleTrackerGroup(
                arguments.getOutputDir(),
                "timings.csv",
                List.of("step", "prehook", "copy", "reassignPointer", "sort", "deliberation", "gatheringActions", "posthook")
        );
    }

    public boolean start() {
        if (this.nIterations <= 0) {
            while(true) {
                this.doStep();
            }
        } else {
            for (int i = 0; i < this.nIterations; ++i) {
                this.doStep();
            }
        }

        this.processSimulationFinishedHook(this.nIterations, this.executor.getLastTimeStepDuration());
        this.executor.shutdown();
        return true;
    }

    private void doStep() {
        int step = this.executor.getCurrentTimeStep();

        HashMap<String, String> timingsMap = new HashMap<>();
        timingsMap.put("step", Integer.toString(this.executor.getCurrentTimeStep()));

        long millis = System.currentTimeMillis();
        this.processStepStarting(step);
        timingsMap.put("prehook", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        List<Future<DeliberationResult<T>>> agentActions = ((NoRescheduleBlockingStepExecutor<T>)this.executor).doTimeStep(timingsMap);
        timingsMap.put("deliberation", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        this.processStepFinished(step, this.executor.getLastTimeStepDuration(), agentActions);
        timingsMap.put("posthook", Long.toString(System.currentTimeMillis() - millis));

        this.timingsTracker.writeKeyMapToFile(
                arguments.getStartdate().plusDays(this.executor.getCurrentTimeStep()),
                timingsMap
        );
    }
}
