package main.java.nl.uu.iss.ga.simulation.agent.context;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.stream.*;

public class LocationHistoryContext implements Context {

    private long lastDayTick = 0;

    private Map<Long, LocationHistory> locationHistory = new HashMap<>();

    public void addVisit(long tick, Visit visit) {
        this.lastDayTick = tick;
        if(!this.locationHistory.containsKey(visit.getLocationID())) {
            this.locationHistory.put(visit.getLocationID(), new LocationHistory(visit.getLocationID()));
        }
        this.locationHistory.get(visit.getLocationID()).addVisit(tick, visit);
    }

    public @Nullable Visit getLastVisit(long locationID) {
        return this.getVisit(locationID, this.lastDayTick);
    }

    public Visit getVisit(long locationID, long tick) {
        return this.locationHistory.get(locationID).getVisit(tick);
    }

    /**
     * Returns a list of visits of the last n ticks.
     * Size of list may be smaller than n, because the agent does not necessarily visit every location at every tick
     * @param n List of visit history
     */
    public List<Visit> getLastDaysVisits(long locationID, int n) {
        List<Visit> visitHistory = new ArrayList<>();
        for(long i = this.lastDayTick - n; i < this.lastDayTick; i++) {
            Visit visit = getVisit(locationID, i);
            if(visit != null) {
                visitHistory.add(visit);
            }
        }
        return visitHistory;
    }

    /*
     * =============================================
     * ||               NORMAL SEEN               ||
     * =============================================
     */

    public int getLastDaySeen() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenAt).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaySeenAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeen(this.lastDayTick) : 0;
    }

    public int getLastDaysSeen(int n) {
        return this.locationHistory.keySet().stream().map(x -> this.getLastDaysSeenAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaysSeenAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeen(this.lastDayTick, n) : 0;
    }

    public double getLastDaysSeenAverage(int n) {
        Stream<Double> s = this.locationHistory.values().stream().map(x -> x.getLastDaysSeenAverage(this.lastDayTick, n)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
    }

    public double getLastDaysSeenAtAverage(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenAverage(this.lastDayTick, n) : 0;
    }

    /*
     * =============================================
     * ||                 MASKS                   ||
     * =============================================
     */

    public int getLastDaySeenMask() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenMaskAt).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaySeenMaskAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenMask(this.lastDayTick) : 0;
    }

    public int getLastDaysSeenMask(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenMaskAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaysSeenMaskAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenMask(this.lastDayTick, n) : 0;
    }

    public double getLastDaysSeenMaskAverage(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysSeenMaskAverageAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0d;
    }

    public double getLastDaysSeenMaskAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenMaskAverage(this.lastDayTick, n) : 0;
    }

    public double getLastDayFractionMask() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionMaskAt);
    }

    public double getLastDayFractionMaskAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionMask(this.lastDayTick) : 0;
    }

    public double getLastDaysFractionMask(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysFractionMaskAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
    }

    public double getLastDaysFractionMaskAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionMask(this.lastDayTick, n) : 0;
    }

    /*
     * =============================================
     * ||               DISTANCING                ||
     * =============================================
     */

    public int getLastDaySeenDistancing() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenDistancingAt).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaySeenDistancingAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenDistancing(this.lastDayTick) : 0;
    }

    public int getLastDaysSeenDistancing(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenDistancingAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaysSeenDistancingAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenDistancing(this.lastDayTick, n) : 0;
    }

    public double getLastDaysSeenDistancingAverage(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysSeenDistancingAverageAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0d;
    }

    public double getLastDaysSeenDistancingAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenDistancingAverage(this.lastDayTick, n) : 0;
    }

    public double getLastDayFractionDistancing() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionDistancingAt);
    }

    public double getLastDayFractionDistancingAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionDistancing(this.lastDayTick) : 0;
    }

    public double getLastDaysFractionDistancing(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysFractionDistancingAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
    }

    public double getLastDaysFractionDistancingAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionDistancing(this.lastDayTick, n) : 0;
    }

    /*
     * =============================================
     * ||              SYMPTOMATIC                ||
     * =============================================
     */

    public int getLastDaySeenSymptomatic() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenSymptomaticAt).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaySeenSymptomaticAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenSymptomatic(this.lastDayTick) : 0;
    }

    public int getLastDaysSeenSymptomatic(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenSymptomaticAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    public int getLastDaysSeenSymptomaticAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenSymptomatic(this.lastDayTick, n) : 0;
    }

    public double getLastDaysSeenSymptomaticAverage(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysSeenSymptomaticAverageAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0d;
    }

    public double getLastDaysSeenSymptomaticAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenSymptomaticAverage(this.lastDayTick, n) : 0;
    }

    public double getLastDayFractionSymptomatic() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionSymptomaticAt);
    }

    public double getLastDayFractionSymptomaticAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionSymptomatic(this.lastDayTick) : 0;
    }

    public double getLastDaysFractionSymptomatic(int n) {
        Stream<Double> s = this.locationHistory.keySet().stream().map(x -> this.getLastDaysFractionSymptomaticAt(n, x)).filter(x -> x != 0d);
        return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
    }

    public double getLastDaysFractionSymptomaticAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionSymptomatic(this.lastDayTick, n) : 0;
    }

    /*
     * =============================================
     * ||                 HELPERS                 ||
     * =============================================
     */

    private double getLastDayFractionForMethod(Function<? super Long,? extends Double> method) {
        List<Double> fractions = this.locationHistory.keySet().stream()
                .map(method).filter(x -> x != 0d).collect(Collectors.toList());
        return fractions.isEmpty() ? 0 : fractions.stream().reduce(Double::sum).orElse(0d)  / fractions.size();
    }

    static class LocationHistory {
        private long locationID;
        private final Map<Long, Visit> locationHistory;

        public LocationHistory(long locationID) {
            this.locationHistory = new TreeMap<>();
        }

        public void addVisit(long tick, Visit visit) {
            this.locationHistory.put(tick, visit);
        }

        public Visit getVisit(long tick) {
            return this.locationHistory.get(tick);
        }

        public long getLocationID() {
            return locationID;
        }

        public int getLastDaySeen(long tick) {
            return this.locationHistory.containsKey(tick) ? this.locationHistory.get(tick).getN_contacts() : 0;
        }

        public int getLastDaysSeen(long lastTick, int n) {
            return getCountForDaysStream(lastTick, n, this::getLastDaySeen).reduce(Integer::sum).orElse(0);
        }

        public double getLastDaysSeenAverage(long lastTick, int n) {
            IntStream s = getCountForDaysStream(lastTick, n, this::getLastDaySeen);
            return s.count() > 0 ? (double) s.reduce(Integer::sum).orElse(0) / s.count() : 0;
        }

        /*
         * ==================================
         *          Masks
         * ==================================
         */

        public int getLastDaySeenMask(long tick) {
            return this.locationHistory.containsKey(tick) ? this.locationHistory.get(tick).getN_mask() : 0;
        }

        public int getLastDaysSeenMask(long lastTick, int n) {
            return getCountForDaysStream(lastTick, n, this::getLastDaySeenMask).reduce(Integer::sum).orElse(0);
        }

        public double getLastDaysSeenMaskAverage(long lastTick, int n) {
            IntStream s = getCountForDaysStream(lastTick, n, this::getLastDaySeenMask);
            return s.count() > 0 ? (double) s.reduce(Integer::sum).orElse(0) / s.count() : 0;
        }

        public double getLastDayFractionMask(long tick) {
            if(this.locationHistory.containsKey(tick)) {
                return (double) this.locationHistory.get(tick).getN_mask() / this.locationHistory.get(tick).getN_contacts();
            } else {
                return 0;
            }
        }

        public double getLastDaysFractionMask(long lastTick, int n) {
            DoubleStream s = getFractionOfSeenStream(lastTick, n, this::getLastDayFractionMask);
            return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
        }

        /*
         * ==================================
         *          Distancing
         * ==================================
         */

        public int getLastDaySeenDistancing(long tick) {
            return this.locationHistory.containsKey(tick) ? this.locationHistory.get(tick).getN_distancing() : 0;
        }

        public int getLastDaysSeenDistancing(long lastTick, int n) {
            return getCountForDaysStream(lastTick, n, this::getLastDaySeenDistancing).reduce(Integer::sum).orElse(0);
        }

        public double getLastDaysSeenDistancingAverage(long lastTick, int n) {
            IntStream s = getCountForDaysStream(lastTick, n, this::getLastDaySeenDistancing);
            return s.count() > 0 ? (double) s.reduce(Integer::sum).orElse(0) / s.count() : 0;
        }

        public double getLastDayFractionDistancing(long tick) {
            if(this.locationHistory.containsKey(tick)) {
                return (double) this.locationHistory.get(tick).getN_distancing() / this.locationHistory.get(tick).getN_contacts();
            } else {
                return 0;
            }
        }

        public double getLastDaysFractionDistancing(long lastTick, int n) {
            DoubleStream s = getFractionOfSeenStream(lastTick, n, this::getLastDayFractionDistancing);
            return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
        }

        /*
         * ==================================
         *          Symptomatic
         * ==================================
         */

        public int getLastDaySeenSymptomatic(long tick) {
            return this.locationHistory.containsKey(tick) ? this.locationHistory.get(tick).getN_symptomatic() : 0;
        }

        public int getLastDaysSeenSymptomatic(long lastTick, int n) {
            return getCountForDaysStream(lastTick, n, this::getLastDaySeenSymptomatic).reduce(Integer::sum).orElse(0);
        }

        public double getLastDaysSeenSymptomaticAverage(long lastTick, int n) {
            IntStream s = getCountForDaysStream(lastTick, n, this::getLastDaySeenSymptomatic);
            return s.count() > 0 ? (double) s.reduce(Integer::sum).orElse(0) / s.count() : 0;
        }

        public double getLastDayFractionSymptomatic(long tick) {
            if(this.locationHistory.containsKey(tick)) {
                return (double) this.locationHistory.get(tick).getN_symptomatic() / this.locationHistory.get(tick).getN_contacts();
            } else {
                return 0;
            }
        }

        public double getLastDaysFractionSymptomatic(long lastTick, int n) {
            DoubleStream s = getFractionOfSeenStream(lastTick, n, this::getLastDayFractionSymptomatic);
            return s.count() > 0 ? s.reduce(Double::sum).orElse(0d) / s.count() : 0;
        }

        /*
         * ==================================
         *          Helpers
         * ==================================
         */

        private IntStream getCountForDaysStream(long lastTick, int n, LongToIntFunction f) {
            return LongStream.rangeClosed(lastTick - n, lastTick)
                    .filter(this.locationHistory::containsKey)
                    .mapToInt(f);
        }

        private DoubleStream getFractionOfSeenStream(long lastTick, int n, LongToDoubleFunction f) {
            return LongStream.rangeClosed(lastTick - n, lastTick)
                    .filter(this.locationHistory::containsKey)
                    .mapToDouble(f)
                    .filter(x -> x != 0d);
        }
    }

    public static class Visit {
        private final long personID;
        private final long locationID;
        private final double infectionProbability;
        private final int n_contacts;
        private final int n_mask;
        private final int n_distancing;
        private final int n_symptomatic;

        public Visit(long personID, long locationID, double infectionProbability, int n_contacts, int n_symptomatic, int n_mask, int n_distancing) {
            this.personID = personID;
            this.locationID = locationID;
            this.infectionProbability = infectionProbability;
            this.n_contacts = n_contacts;
            this.n_mask = n_mask;
            this.n_distancing = n_distancing;
            this.n_symptomatic = n_symptomatic;
        }

        public long getPersonID() {
            return personID;
        }

        public long getLocationID() {
            return locationID;
        }

        public double getInfectionProbability() {
            return infectionProbability;
        }

        public int getN_contacts() {
            return n_contacts;
        }

        public int getN_mask() {
            return n_mask;
        }

        public int getN_distancing() {
            return n_distancing;
        }

        public int getN_symptomatic() {
            return n_symptomatic;
        }
    }
}
