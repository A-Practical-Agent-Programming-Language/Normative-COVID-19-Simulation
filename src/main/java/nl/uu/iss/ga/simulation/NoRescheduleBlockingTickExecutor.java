package main.java.nl.uu.iss.ga.simulation;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationRunnable;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class NoRescheduleBlockingTickExecutor<T> implements TickExecutor<T> {

    private int tick;
    private int stepDuration;
    private Random random;
    private final ExecutorService executor;

    private Queue<DeliberationRunnable<T>> scheduledRunnables;

    public NoRescheduleBlockingTickExecutor(int nThreads) {
        this.tick = 0;
        this.executor = Executors.newFixedThreadPool(nThreads);
        this.scheduledRunnables = new ConcurrentLinkedQueue<>();
    }

    public NoRescheduleBlockingTickExecutor(int nThreads, Random random) {
        this(nThreads);
        this.random = random;
    }

    public <X> List<Future<X>> useExecutorForTasks(Collection<? extends Callable<X>> tasks) throws InterruptedException {
        return this.executor.invokeAll(tasks);
    }

    public boolean scheduleForNextTick(DeliberationRunnable<T> agentDeliberationRunnable) {
        this.scheduledRunnables.add(agentDeliberationRunnable);
        return true;
    }

    @Override
    public HashMap<AgentID, List<T>> doTick() {
//        return doTick(new HashMap<>());
        return null;
    }

    public List<Future<DeliberationResult<T>>> doTick(HashMap<String, String> timingsMap) {

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
        ++this.tick;
//        return agentPlanActions;
        return currentAgentFutures;
    }

    public int getCurrentTick() {
        return this.tick;
    }

    public boolean isRunning() {
        return false;
    }

    public int getLastTickDuration() {
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
