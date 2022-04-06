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
        update(locationHistoryContext, seen, symptomatic, behavior);

        return locationHistoryContext;
    }

    public static void update(LocationHistoryContext locationHistoryContext, int seen, int symptomatic, int behavior) {
        when(locationHistoryContext.getLastDaySeen()).thenReturn(seen);
        when(locationHistoryContext.getLastDaySeenSymptomatic()).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaySeenMask()).thenReturn(behavior);
        when(locationHistoryContext.getLastDaySeenDistancing()).thenReturn(behavior);

        when(locationHistoryContext.getLastDaySeenAt(anyLong())).thenReturn(seen);
        when(locationHistoryContext.getLastDaySeenSymptomaticAt(anyLong())).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaySeenMaskAt(anyLong())).thenReturn(behavior);
        when(locationHistoryContext.getLastDaySeenDistancingAt(anyLong())).thenReturn(behavior);

        when(locationHistoryContext.getLastDaysSeen(anyInt())).thenReturn(seen);
        when(locationHistoryContext.getLastDaysSeenSymptomatic(anyInt())).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaysSeenMask(anyInt())).thenReturn(behavior);
        when(locationHistoryContext.getLastDaysSeenDistancing(anyInt())).thenReturn(behavior);

        when(locationHistoryContext.getLastDaysSeenAt(anyInt(), anyLong())).thenReturn(seen);
        when(locationHistoryContext.getLastDaysSeenSymptomaticAt(anyInt(), anyLong())).thenReturn(symptomatic);
        when(locationHistoryContext.getLastDaysSeenDistancingAt(anyInt(), anyLong())).thenReturn(behavior);
        when(locationHistoryContext.getLastDaysSeenMaskAt(anyInt(), anyLong())).thenReturn(behavior);

        when(locationHistoryContext.getLastDayFractionDistancing()).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionMask()).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionSymptomatic()).thenReturn((double) symptomatic / seen);

        when(locationHistoryContext.getLastDayFractionDistancingAt(anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionMaskAt(anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDayFractionSymptomaticAt(anyLong())).thenReturn((double) symptomatic / seen);

        when(locationHistoryContext.getLastDaysFractionDistancing(anyInt())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionMask(anyInt())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionSymptomatic(anyInt())).thenReturn((double) symptomatic / seen);

        when(locationHistoryContext.getLastDaysFractionDistancingAt(anyInt(), anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionMaskAt(anyInt(), anyLong())).thenReturn((double) behavior / seen);
        when(locationHistoryContext.getLastDaysFractionSymptomaticAt(anyInt(), anyLong())).thenReturn((double)  symptomatic / seen);

        when(locationHistoryContext.getLastDaysSeenAverage(anyInt())).thenReturn((double)seen);
        when(locationHistoryContext.getLastDaysSeenDistancingAverage(anyInt())).thenReturn((double)behavior);
        when(locationHistoryContext.getLastDaysSeenMaskAverage(anyInt())).thenReturn((double)behavior);
        when(locationHistoryContext.getLastDaysSeenSymptomaticAverage(anyInt())).thenReturn((double)symptomatic);

        when(locationHistoryContext.getLastDaysSeenAtAverage(anyInt(), anyLong())).thenReturn((double)seen);
        when(locationHistoryContext.getLastDaysSeenDistancingAverageAt(anyInt(), anyLong())).thenReturn((double)behavior);
        when(locationHistoryContext.getLastDaysSeenMaskAverageAt(anyInt(), anyLong())).thenReturn((double)behavior);
        when(locationHistoryContext.getLastDaysSeenSymptomaticAverageAt(anyInt(), anyLong())).thenReturn((double)symptomatic);
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
