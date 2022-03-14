package nl.uu.iss.ga.mock;

import nl.uu.iss.ga.simulation.agent.context.BeliefContext;
import nl.uu.iss.ga.simulation.agent.context.LocationHistoryContext;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MockLocationHistoryContext {

    public static LocationHistoryContext getLocationHistoryContext(boolean simulateViolation) {
        int seen = 100;
        int symptomatic = simulateViolation ? 50 : 0;
        int behavior = simulateViolation ? 0 : 100;

        return getLocationHistoryContext(seen, symptomatic, behavior);
    }

    public static LocationHistoryContext getLocationHistoryContext(int seen, int symptomatic, int behavior) {
        BeliefContext beliefContext = mock(BeliefContext.class);
        when(beliefContext.getPriorTrustAttitude()).thenReturn(0d);

        LocationHistoryContext locationHistoryContext = mock(LocationHistoryContext.class);

        when(locationHistoryContext.getLastDaysSeenAt(anyInt(), anyLong())).thenReturn(seen);
        when(locationHistoryContext.getLastDaysSeenSymptomaticAt(anyInt(), anyLong())).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaysSeenDistancingAt(anyInt(), anyLong())).thenReturn(behavior);
        when(locationHistoryContext.getLastDaysSeenMaskAt(anyInt(), anyLong())).thenReturn(behavior);

        when(locationHistoryContext.getLastDaySeen()).thenReturn(seen);
        when(locationHistoryContext.getLastDaySeenSymptomatic()).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaySeenMask()).thenReturn(behavior);
        when(locationHistoryContext.getLastDaySeenDistancing()).thenReturn(behavior);

        when(locationHistoryContext.getLastDayFractionDistancing()).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionMask()).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionSymptomatic()).thenReturn((double) symptomatic / seen);

        when(locationHistoryContext.getLastDaysFractionDistancing(anyInt())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionMask(anyInt())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionSymptomatic(anyInt())).thenReturn((double) symptomatic / seen);

        when(locationHistoryContext.getLastDaysFractionDistancingAt(anyInt(), anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionMaskAt(anyInt(), anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionSymptomaticAt(anyInt(), anyLong())).thenReturn((double)  symptomatic / seen);

        return locationHistoryContext;
    }

    @Test
    public void testLocationHistoryAccurate() {
        LocationHistoryContext context = getLocationHistoryContext(true);

        assertEquals(0d, context.getLastDayFractionDistancing());
        assertEquals(0d, context.getLastDayFractionMask());
        assertEquals(.5d, context.getLastDayFractionSymptomatic());
        for(int i = 0; i < 20; i++) {
            assertEquals(0d, context.getLastDaysFractionDistancing(i));
            assertEquals(0d, context.getLastDaysFractionMask(i));
            assertEquals(.5d, context.getLastDaysFractionSymptomatic(i));
        }
    }

}
