package main.java.nl.uu.iss.ga.simulation;

import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.tracking.ScheduleTrackerGroup;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.AbstractSimulationEngine;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Future;

public class DefaultTimingSimulationEngine<T> extends AbstractSimulationEngine<T> {
    private final TickExecutor<T> executor;
    private final ScheduleTrackerGroup timingsTracker;
    private final ArgParse arguments;

    public DefaultTimingSimulationEngine(Platform platform, ArgParse arguments, int nIterations, TickHookProcessor<T>... hookProcessors) {
        super(platform, nIterations, hookProcessors);
        this.executor = platform.getTickExecutor();
        this.arguments = arguments;

        Path outputDir = Path.of(arguments.getOutputDir()).isAbsolute() ?
                Path.of(arguments.getOutputDir()) : Path.of("output", arguments.getOutputDir());

        this.timingsTracker = new ScheduleTrackerGroup(
                outputDir.toFile().getAbsolutePath(),
                "timings.csv",
                List.of("tick", "prehook", "copy", "reassignPointer", "sort", "deliberation", "gatheringActions", "posthook")
        );
    }

    public boolean start() {
        if (this.nIterations <= 0) {
            while(true) {
                this.doTick();
            }
        } else {
            for (int i = 0; i < this.nIterations; ++i) {
                this.doTick();
            }
        }

        this.processSimulationFinishedHook(this.nIterations, this.executor.getLastTickDuration());
        this.executor.shutdown();
        return true;
    }

    private void doTick() {
        int tick = this.executor.getCurrentTick();

        HashMap<String, String> timingsMap = new HashMap<>();
        timingsMap.put("tick", Integer.toString(this.executor.getCurrentTick()));

        long millis = System.currentTimeMillis();
        this.processTickPreHooks(tick);
        timingsMap.put("prehook", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        List<Future<DeliberationResult<T>>> agentActions = ((NoRescheduleBlockingTickExecutor<T>)this.executor).doTick(timingsMap);
        timingsMap.put("deliberation", Long.toString(System.currentTimeMillis() - millis));
        millis = System.currentTimeMillis();

        this.processTickPostHook(tick, this.executor.getLastTickDuration(), agentActions);
        timingsMap.put("posthook", Long.toString(System.currentTimeMillis() - millis));

        this.timingsTracker.writeKeyMapToFile(
                arguments.getStartdate().plusDays(this.executor.getCurrentTick()),
                timingsMap
        );
    }
}
