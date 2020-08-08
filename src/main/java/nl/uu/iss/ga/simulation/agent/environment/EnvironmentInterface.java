package main.java.nl.uu.iss.ga.simulation.agent.environment;

import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.util.HashMap;
import java.util.List;

public class EnvironmentInterface implements TickHookProcessor {

    long currentTick = 0;
    DayOfWeek today = DayOfWeek.SUNDAY;

    public long getCurrentTick() {
        return currentTick;
    }

    public DayOfWeek getToday() {
        return today;
    }

    @Override
    public void tickPreHook(long l) {
        this.currentTick = l;
        this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int)(currentTick % 7 + 1));
    }

    @Override
    public void tickPostHook(long l, int i, HashMap<AgentID, List<String>> hashMap) {
        System.out.printf("Day %d. %d agents have actions", l, hashMap.size());
        for(AgentID aid : hashMap.keySet()) {
            System.out.printf("\t%s has %d activities: %s\n", aid.getName().getUserInfo(), hashMap.get(aid).size(), hashMap.get(aid).toString());
        }
        System.out.println("\n\n");
    }

    @Override
    public void simulationFinishedHook(long l, int i) {
        System.exit(0);
    }
}
