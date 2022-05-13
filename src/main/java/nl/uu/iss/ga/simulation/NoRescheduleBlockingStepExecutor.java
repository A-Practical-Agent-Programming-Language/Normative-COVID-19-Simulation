package nl.uu.iss.ga.simulation;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationRunnable;
import nl.uu.cs.iss.ga.sim2apl.core.step.StepExecutor;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class NoRescheduleBlockingStepExecutor<T> implements StepExecutor<T> {

    private int timeStep;
    private int stepDuration;
    private Random random;
    private final ExecutorService executor;

    private Queue<DeliberationRunnable<T>> scheduledRunnables;

    public NoRescheduleBlockingStepExecutor(int nThreads) {
        this.timeStep = 0;
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.scheduledRunnables = new ConcurrentLinkedQueue<>();
    }

    public NoRescheduleBlockingStepExecutor(int nThreads, Random random) {
        this(nThreads);
        this.random = random;
    }

    public <X> List<Future<X>> useExecutorForTasks(Collection<? extends Callable<X>> tasks) throws InterruptedException {
        return this.executor.invokeAll(tasks);
    }

    public boolean scheduleForNextTimeStep(DeliberationRunnable<T> agentDeliberationRunnable) {
        this.scheduledRunnables.add(agentDeliberationRunnable);
        return true;
    }

    @Override
    public List<Future<DeliberationResult<T>>> doTimeStep() {
        return doTimeStep(new HashMap<>());
    }

    public List<Future<DeliberationResult<T>>> doTimeStep(HashMap<String, String> timingsMap) {

        long millis = System.currentTimeMillis();
        Queue<DeliberationRunnable<T>> runnables;
        runnables = this.scheduledRunnables;
        this.scheduledRunnables = new ConcurrentLinkedQueue<>();
        timingsMap.put("reassignPointer", Long.toString(System.currentTimeMillis() - millis));

        // TODO this would be an interesting test to perform, but we get exceptions left and right?

//        millis = System.currentTimeMillis();
//        if (this.random != null) {
//            runnables.sort(Comparator.comparing((deliberationRunnable) -> deliberationRunnable != null && deliberationRunnable.getAgentID() != null ? deliberationRunnable.getAgentID().getUuID() : ""));
//            Collections.shuffle(runnables, this.random);
//        }
//        timingsMap.put("sort", Long.toString(System.currentTimeMillis() - millis));

        HashMap<AgentID, List<T>> agentPlanActions = null;
        List<Future<DeliberationResult<T>>> currentAgentFutures = null;
        long startTime = System.currentTimeMillis();

        try {
            millis = System.currentTimeMillis();
            currentAgentFutures = this.executor.invokeAll(runnables);
            timingsMap.put("deliberation", Long.toString(System.currentTimeMillis() - millis));

//            millis = System.currentTimeMillis();
//            agentPlanActions = new HashMap<>(currentAgentFutures.size());
//            for(Future<DeliberationResult<T>> futureResult : currentAgentFutures) {
//                DeliberationResult<T> result = futureResult.get();
//                agentPlanActions.put(result.getAgentID(), result.getActions().stream().filter(Objects::nonNull).collect(Collectors.toList()));
//            }
//            timingsMap.put("gatheringActions", Long.toString(System.currentTimeMillis() - millis));

        } catch (InterruptedException var8) {
            var8.printStackTrace();
        }

        this.stepDuration = (int)(System.currentTimeMillis() - startTime);
        ++this.timeStep;
//        return agentPlanActions;
        return currentAgentFutures;
    }

    public int getCurrentTimeStep() {
        return this.timeStep;
    }

    public boolean isRunning() {
        return false;
    }

    public int getLastTimeStepDuration() {
        return this.stepDuration;
    }

    public List<AgentID> getScheduledAgents() {
        return this.scheduledRunnables.stream().map(DeliberationRunnable::getAgentID).collect(Collectors.toList());
    }

    public int getNofScheduledAgents() {
        return this.scheduledRunnables.size();
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}
