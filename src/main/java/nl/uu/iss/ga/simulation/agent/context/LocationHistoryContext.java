package nl.uu.iss.ga.simulation.agent.context;

import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.LongToDoubleFunction;
import java.util.function.LongToIntFunction;
import java.util.function.Supplier;
import java.util.logging.Logger;
import java.util.stream.*;

public class LocationHistoryContext implements Context {

    private static final Logger LOGGER = Logger.getLogger(LocationHistoryContext.class.getSimpleName());

    private long lastDayTimeStep = 0;

    private Map<Long, LocationHistory> locationHistory = new ConcurrentHashMap<>();

    /**
     * Add a new visit to the agent visit history
     * @param timeStep      Time step of the visit
     * @param visit     Visit
     */
    public void addVisit(long timeStep, Visit visit) {
        this.lastDayTimeStep = timeStep;
        if(!this.locationHistory.containsKey(visit.getLocationID())) {
            this.locationHistory.put(visit.getLocationID(), new LocationHistory(visit.getLocationID()));
        }
        this.locationHistory.get(visit.getLocationID()).addVisit(timeStep, visit);
    }

    public void setLastDayTimeStep(int lastDayTimeStep) {
        this.lastDayTimeStep = lastDayTimeStep;
    }

    /**
     * Get last time step's visit from the agent visit history
     *
     * @param locationID    ID of the location for which to find the visit of the last day
     * @return              Last time step's visit object for location <code>locationID</code>, or null if the
     *                      agent did not visit the location during the last time step
     */
    public @Nullable Visit getLastVisit(long locationID) {
        return this.getVisit(locationID, this.lastDayTimeStep);
    }

    /**
     * Get a visit from a specific time step
     * @param locationID    ID of the location for which to find the visit of the specified time step
     * @param timeStep          Time step for which to find the visit object
     * @return              Visit object for specified location on specified time step, or null if the agent did not
     *                      visit the location during that time step
     */
    public Visit getVisit(long locationID, long timeStep) {
        return this.locationHistory.get(locationID).getVisit(timeStep);
    }

    /**
     * Get a list of visits of the last n time steps.
     * Note: Size of list may be smaller than n, because the agent does not necessarily visit every location at every time step
     *
     * @param locationID    Location ID for which to get visit information
     * @param n             Number of time steps to look back
     * @return              List of visits at the specified location between the current time step, and the current timestep
     *                      minus <code>n</code>
     */
    public List<Visit> getLastDaysVisits(long locationID, int n) {
        List<Visit> visitHistory = new ArrayList<>();
        for(long i = this.lastDayTimeStep - n; i < this.lastDayTimeStep; i++) {
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

    /**
     * Get the number of other agents the current agent encountered on all its visits during the last time step
     * @return  Number of agents encountered
     */
    public int getLastDaySeen() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenAt).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last time step.
     * This value may be 0 both if the current agent was alone at the specified location, or if the agent did not visit
     * the specified location during the last time step
     *
     * @param locationID Location ID for which to get visit information
     * @return  Number of encountered agent at the specified location
     */
    public int getLastDaySeenAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeen(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the number of other agents the current agent encountered on all its visits during the last <code>n</code> time steps
     * @param n Number of time steps to look back
     * @return  Total number of encountered agents at all visited locations during the last <code>n</code> time steps
     */
    public int getLastDaysSeen(int n) {
        return this.locationHistory.keySet().stream().map(x -> this.getLastDaysSeenAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last <code>n</code> time steps.
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps, or if
     * the agent did not visit the specified location in the specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Number of encountered agent at the specified location during the specified time steps
     */
    public int getLastDaysSeenAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeen(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps
     *
     * @param n Number of time steps to look back
     * @return  Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     */
    public double getLastDaysSeenAverage(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.values().stream()
                .map(x -> x.getLastDaysSeenAverage(this.lastDayTimeStep, n)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps at
     * the specified location
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps, or if
     * the agent did not visit the specified location in the specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     */
    public double getLastDaysSeenAtAverage(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenAverage(this.lastDayTimeStep, n) : 0;
    }

    /*
     * =============================================
     * ||                 MASKS                   ||
     * =============================================
     */

    /**
     * Get the number of other agents the current agent encountered who were seen wearing a mask on all its visits during
     * the last time step
     * @return  Number of agents encountered who were seen wearing a mask
     */
    public int getLastDaySeenMask() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenMaskAt).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last time step who
     * were seen wearing a mask.
     * This value may be 0 both if the current agent was alone at the specified location, none of the other agents wore
     * a mask, or if the agent did not visit the specified location during the last time step
     *
     * @param locationID Location ID for which to get visit information
     * @return  Number of encountered agent at the specified location who were seen wearing a mask
     */
    public int getLastDaySeenMaskAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenMask(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the number of other agents the current agent encountered on all its visits during the last <code>n</code> time steps
     * who were seen wearing masks
     * @param n Number of time steps to look back
     * @return  Total number of encountered agents at all visited locations during the last <code>n</code> time steps who
     * were seen wearing a mask
     */
    public int getLastDaysSeenMask(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenMaskAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last <code>n</code>
     * time steps who were seen wearing a mask.
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents were seen wearing a mask, or if the agent did not visit the specified location in the
     * specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Number of encountered agent at the specified location during the specified time steps who were seen
     * wearing a mask
     */
    public int getLastDaysSeenMaskAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenMask(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps who were
     * seen wearing a mask
     *
     * @param n Number of time steps to look back
     * @return  Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were seen wearing a mask
     */
    public double getLastDaysSeenMaskAverage(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysSeenMaskAverageAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0d;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps at
     * the specified location who were seen wearing a mask
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents wore a mask, or if the agent did not visit the specified location in the specified
     * time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were seen wearing a mask
     */
    public double getLastDaysSeenMaskAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenMaskAverage(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were seen wearing
     * a mask.
     *
     * @return Fraction of total number of encountered agents seen wearing a mask
     */
    public double getLastDayFractionMask() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionMaskAt);
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were seen wearing
     * masks at the specified location
     *
     * @return Fraction of total number of encountered agents seen wearing a mask at the specified location
     */
    public double getLastDayFractionMaskAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionMask(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps who were
     * seen wearing a mask.
     *
     * @return Fraction of total number of encountered agents during the last <code>n</code> time steps seen wearing a mask
     */
    public double getLastDaysFractionMask(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysFractionMaskAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps at the
     * specified location who were seen wearing a mask.
     *
     * @return Fraction of total number of encountered agents at the specified location during the last <code>n</code>
     * time steps seen wearing a mask
     */
    public double getLastDaysFractionMaskAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionMask(this.lastDayTimeStep, n) : 0;
    }

    /*
     * =============================================
     * ||               DISTANCING                ||
     * =============================================
     */

    /**
     * Get the number of other agents the current agent encountered who were seen practicing physical distancing on all
     * its visits during the last time step
     * @return  Number of agents encountered who were seen practicing physical distancing
     */
    public int getLastDaySeenDistancing() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenDistancingAt).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last time step who
     * were seen practicing physical distancing.
     * This value may be 0 both if the current agent was alone at the specified location, none of the other agents practiced
     * physical distancing, or if the agent did not visit the specified location during the last time step
     *
     * @param locationID Location ID for which to get visit information
     * @return  Number of encountered agent at the specified location who were seen practicing physical distancing
     */
    public int getLastDaySeenDistancingAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenDistancing(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the number of other agents the current agent encountered on all its visits during the last <code>n</code> time steps
     * who were seen practicing physical distancing
     * @param n Number of time steps to look back
     * @return  Total number of encountered agents at all visited locations during the last <code>n</code> time steps who
     * were seen practicing physical distancing
     */
    public int getLastDaysSeenDistancing(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenDistancingAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last <code>n</code>
     * time steps who were seen practicing physical distancing.
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents were practicing physical distancing, or if the agent did not visit the specified location in the
     * specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Number of encountered agent at the specified location during the specified time steps who were seen
     * practicing physical distancing
     */
    public int getLastDaysSeenDistancingAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenDistancing(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps who were
     * seen practicing physical distancing
     *
     * @param n Number of time steps to look back
     * @return  Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were seen practicing physical distancing
     */
    public double getLastDaysSeenDistancingAverage(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysSeenDistancingAverageAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0d;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps at
     * the specified location who were seen practicing physical distancing
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents were practicing physical distancing, or if the agent did not visit the specified
     * location in the specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were seen practicing physical distancing
     */
    public double getLastDaysSeenDistancingAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenDistancingAverage(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were
     * seen practicing physical distancing.
     *
     * @return Fraction of total number of encountered agents seen practicing physical distancing
     */
    public double getLastDayFractionDistancing() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionDistancingAt);
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were
     * seen practicing physical distancing at the specified location
     *
     * @return Fraction of total number of encountered agents seen practicing physical distancing at the specified location
     */
    public double getLastDayFractionDistancingAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionDistancing(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps who were
     * seen practicing physical distancing.
     *
     * @return Fraction of total number of encountered agents during the last <code>n</code> time steps seen
     * practicing physical distancing
     */
    public double getLastDaysFractionDistancing(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysFractionDistancingAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps at the
     * specified location who were seen practicing physical distancing.
     *
     * @return Fraction of total number of encountered agents at the specified location during the last <code>n</code>
     * time steps seen practicing physical distancing
     */
    public double getLastDaysFractionDistancingAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionDistancing(this.lastDayTimeStep, n) : 0;
    }

    /*
     * =============================================
     * ||              SYMPTOMATIC                ||
     * =============================================
     */

    /**
     * Get the number of other agents the current agent encountered who were visibly symptomatic on all its visits during
     * the last time step
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @return  Number of agents encountered who were visibly symptomatic
     */
    public int getLastDaySeenSymptomatic() {
        return this.locationHistory.keySet().stream().map(this::getLastDaySeenSymptomaticAt).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last time step who
     * were visibly symptomatic.
     * This value may be 0 both if the current agent was alone at the specified location, none of the other agents were
     * visibly symptomatic, or if the agent did not visit the specified location during the last time step
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @param locationID Location ID for which to get visit information
     * @return  Number of encountered agent at the specified location who were visibly symptomatic
     */
    public int getLastDaySeenSymptomaticAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDaySeenSymptomatic(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the number of other agents the current agent encountered on all its visits during the last <code>n</code> time steps
     * who were visibly symptomatic
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @param n Number of time steps to look back
     * @return  Total number of encountered agents at all visited locations during the last <code>n</code> time steps who
     * were visibly symptomatic
     */
    public int getLastDaysSeenSymptomatic(int n) {
        return this.locationHistory.keySet().stream().map(x -> getLastDaysSeenSymptomaticAt(n, x)).reduce(Integer::sum).orElse(0);
    }

    /**
     * Get the number of agents the current agent encountered at the specified location during the last <code>n</code>
     * time steps who were visibly symptomatic.
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents were visibly symptomatic, or if the agent did not visit the specified location in the
     * specified time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Number of encountered agent at the specified location during the specified time steps who were
     * visibly symptomatic
     */
    public int getLastDaysSeenSymptomaticAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenSymptomatic(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps who were
     * visibly symptomatic
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @param n Number of time steps to look back
     * @return  Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were visibly symptomatic
     */
    public double getLastDaysSeenSymptomaticAverage(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysSeenSymptomaticAverageAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0d;
    }

    /**
     * Get the average number of agents the current agent encountered during the last <code>n</code> time steps at
     * the specified location who were visibly symptomatic
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * Note: This value may be 0 both if the current agent was alone at the specified location during all time steps,
     * none of the other agents were visibly symptomatic, or if the agent did not visit the specified location in the specified
     * time steps
     *
     * @param n Number of time steps to look back
     * @param locationID Location ID for which to get visit information
     * @return Average number of encountered agents at all locations visited during the last <code>n</code> time steps
     * who were visibly symptomatic
     */
    public double getLastDaysSeenSymptomaticAverageAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysSeenSymptomaticAverage(this.lastDayTimeStep, n) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were visibly symptomatic
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @return Fraction of total number of encountered agents who were visibly symptomatic
     */
    public double getLastDayFractionSymptomatic() {
        return this.getLastDayFractionForMethod(this::getLastDayFractionSymptomaticAt);
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last time step who were
     * visibly symptomatic at the specified location
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @return Fraction of total number of encountered agents who were visibly symptomatic at the specified location
     */
    public double getLastDayFractionSymptomaticAt(long locationID) {
        return this.locationHistory.containsKey(locationID) ? this.locationHistory.get(locationID).getLastDayFractionSymptomatic(this.lastDayTimeStep) : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps who were
     * visibly symptomatic.
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @return Fraction of total number of encountered agents during the last <code>n</code> time steps who were
     * visibly symptomatic
     */
    public double getLastDaysFractionSymptomatic(int n) {
        Supplier<Stream<Double>> s = () -> this.locationHistory.keySet().stream()
                .map(x -> this.getLastDaysFractionSymptomaticAt(n, x)).filter(x -> x != 0d);
        return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
    }

    /**
     * Get the fraction of the total encountered number of agents seen during the last <code>n</code> time steps at the
     * specified location who were visibly symptomatic.
     *
     * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
     *
     * @return Fraction of total number of encountered agents at the specified location during the last <code>n</code>
     * time steps who were visibly symptomatic
     */
    public double getLastDaysFractionSymptomaticAt(int n, long locationID) {
        return this.locationHistory.containsKey(locationID) ?
                this.locationHistory.get(locationID).getLastDaysFractionSymptomatic(this.lastDayTimeStep, n) : 0;
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

    public static class LocationHistory {
        private long locationID;
        private final Map<Long, Visit> locationHistory;

        /**
         * Construct an empty LocationHistory object
         * @param locationID    LocationID for this object
         */
        public LocationHistory(long locationID) {
            this.locationHistory = new TreeMap<>();
        }

        /**
         * Register a visit to the location of this object
         * @param timeStep  Time step the visit took place
         * @param visit Visit object to register
         */
        public void addVisit(long timeStep, Visit visit) {
            this.locationHistory.put(timeStep, visit);
        }

        /**
         * Get a visit for this location at the specified time step.
         *
         * @param timeStep  Time step the visit took place
         * @return      Visit information, or null if the agent did not visit this location at the specified time step
         */
        public Visit getVisit(long timeStep) {
            return this.locationHistory.get(timeStep);
        }

        /**
         * Get the location ID of this LocationHistory object
         * @return  Location ID
         */
        public long getLocationID() {
            return locationID;
        }

        /**
         * Get the number of agents encountered at the specified time step
         * @param timeStep  Time step
         * @return  Number of agents encountered at the specified time step, or 0 if the agent did not visit this
         * location during the specified time step
         */
        public int getLastDaySeen(long timeStep) {
            return this.locationHistory.containsKey(timeStep) ? this.locationHistory.get(timeStep).getN_contacts() : 0;
        }

        /**
         * Get the number of agents encountered at this location in the specified range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Number of agents encountered at this location in the specified range of time steps
         */
        public int getLastDaysSeen(long lastTimeStep, int n) {
            return getCountForDaysStream(lastTimeStep, n, this::getLastDaySeen).get().reduce(Integer::sum).orElse(0);
        }

        /**
         * Get the average number of agents encountered at this location in the specified range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents encountered at this location in the specified range of time steps
         */
        public double getLastDaysSeenAverage(long lastTimeStep, int n) {
            Supplier<IntStream> s = getCountForDaysStream(lastTimeStep, n, this::getLastDaySeen);
            return s.get().count() > 0 ? (double) s.get().reduce(Integer::sum).orElse(0) / s.get().count() : 0;
        }

        /*
         * ==================================
         *          Masks
         * ==================================
         */

        /**
         * Get the number of agents encountered at the specified time step seen wearing a mask
         * @param timeStep  Time step
         * @return  Number of agents encountered at the specified time step seen wearing a mask,
         * or 0 if the agent did not visit this
         * location during the specified time step
         */
        public int getLastDaySeenMask(long timeStep) {
            return this.locationHistory.containsKey(timeStep) ? this.locationHistory.get(timeStep).getN_mask() : 0;
        }

        /**
         * Get the number of agents encountered at this location seen wearing a mask in the specified range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Number of agents seen wearing a mask at this location in the specified range of time steps
         */
        public int getLastDaysSeenMask(long lastTimeStep, int n) {
            return getCountForDaysStream(lastTimeStep, n, this::getLastDaySeenMask).get().reduce(Integer::sum).orElse(0);
        }

        /**
         * Get the average number of agents seen wearing a mask at this location in the specified
         * range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents seen wearing a mask at this location in the specified range of time steps
         */
        public double getLastDaysSeenMaskAverage(long lastTimeStep, int n) {
            Supplier<IntStream> s = getCountForDaysStream(lastTimeStep, n, this::getLastDaySeenMask);
            return s.get().count() > 0 ? (double) s.get().reduce(Integer::sum).orElse(0) / s.get().count() : 0;
        }

        /**
         * Get the fraction of agents seen wearing a mask of the overall number of encountered agents at this location
         * at the specified time step.
         *
         * @param timeStep The time step
         * @return The fraction of agents of the overall number of encountered agents at this location that was seen
         * wearing a mask
         */
        public double getLastDayFractionMask(long timeStep) {
            if(this.locationHistory.containsKey(timeStep) && this.locationHistory.get(timeStep).getN_contacts() > 0) {
                return (double) this.locationHistory.get(timeStep).getN_mask() / this.locationHistory.get(timeStep).getN_contacts();
            } else {
                return 0;
            }
        }

        /**
         * Get the average fraction of agents seen wearing a mask at this location in the specified
         * range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents seen wearing a mask at this location in the specified range of time steps
         */
        public double getLastDaysFractionMask(long lastTimeStep, int n) {
            Supplier<DoubleStream> s = getFractionOfSeenStream(lastTimeStep, n, this::getLastDayFractionMask);
            return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
        }

        /*
         * ==================================
         *          Distancing
         * ==================================
         */

        /**
         * Get the number of agents encountered at the specified time step seen practicing physical distancing
         * @param timeStep  Time step
         * @return  Number of agents encountered at the specified time step seen practicing physical distancing,
         * or 0 if the agent did not visit this
         * location during the specified time step
         */
        public int getLastDaySeenDistancing(long timeStep) {
            return this.locationHistory.containsKey(timeStep) ? this.locationHistory.get(timeStep).getN_distancing() : 0;
        }

        /**
         * Get the number of agents encountered at this location seen practicing physical distancing in the specified range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Number of agents seen practicing physical distancing at this location in the specified range of time steps
         */
        public int getLastDaysSeenDistancing(long lastTimeStep, int n) {
            return getCountForDaysStream(lastTimeStep, n, this::getLastDaySeenDistancing).get().reduce(Integer::sum).orElse(0);
        }

        /**
         * Get the average number of agents seen practicing physical distancing at this location in the specified
         * range of time steps
         * @param lastTImeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents seen practicing physical distancing at
         * this location in the specified range of time steps
         */
        public double getLastDaysSeenDistancingAverage(long lastTImeStep, int n) {
            Supplier<IntStream> s = getCountForDaysStream(lastTImeStep, n, this::getLastDaySeenDistancing);
            return s.get().count() > 0 ? (double) s.get().reduce(Integer::sum).orElse(0) / s.get().count() : 0;
        }

        /**
         * Get the fraction of agents seen practicing physical distancing of the overall number of encountered agents at this location
         * at the specified time step.
         *
         * @param timeStep The time step
         * @return The fraction of agents of the overall number of encountered agents at this location that was seen
         * practicing physical distancing
         */
        public double getLastDayFractionDistancing(long timeStep) {
            if(this.locationHistory.containsKey(timeStep) && this.locationHistory.get(timeStep).getN_contacts() > 0) {
                return (double) this.locationHistory.get(timeStep).getN_distancing() / this.locationHistory.get(timeStep).getN_contacts();
            } else {
                return 0;
            }
        }

        /**
         * Get the average fraction of agents seen practicing physical distancing at this location in the specified
         * range of time steps
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents seen practicing physical distancing at this location in the specified range of time steps
         */
        public double getLastDaysFractionDistancing(long lastTimeStep, int n) {
            Supplier<DoubleStream> s = getFractionOfSeenStream(lastTimeStep, n, this::getLastDayFractionDistancing);
            return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
        }

        /*
         * ==================================
         *          Symptomatic
         * ==================================
         */

        /**
         * Get the number of agents encountered at the specified time step that were visibly symptomatic
         *
         * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
         *
         * @param timeStep  Time step
         * @return  Number of agents encountered at the specified time step that were visibly symptomatic,
         * or 0 if the agent did not visit this
         * location during the specified time step
         */
        public int getLastDaySeenSymptomatic(long timeStep) {
            return this.locationHistory.containsKey(timeStep) ? this.locationHistory.get(timeStep).getN_symptomatic() : 0;
        }

        /**
         * Get the number of agents encountered at this location that were visibly symptomatic in the specified range of time steps
         *
         * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
         *
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Number of agents that were visibly symptomatic at this location in the specified range of time steps
         */
        public int getLastDaysSeenSymptomatic(long lastTimeStep, int n) {
            return getCountForDaysStream(lastTimeStep, n, this::getLastDaySeenSymptomatic).get().reduce(Integer::sum).orElse(0);
        }

        /**
         * Get the average number of agents that were visibly symptomatic at this location in the specified
         * range of time steps
         *
         * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
         *
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents that were visibly symptomatic at this location in the specified range of time steps
         */
        public double getLastDaysSeenSymptomaticAverage(long lastTimeStep, int n) {
            Supplier<IntStream> s = getCountForDaysStream(lastTimeStep, n, this::getLastDaySeenSymptomatic);
            return s.get().count() > 0 ? (double) s.get().reduce(Integer::sum).orElse(0) / s.get().count() : 0;
        }

        /**
         * Get the fraction of agents that were visibly symptomatic of the overall number of encountered agents at this location
         * at the specified time step.
         *
         * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
         *
         * @param timeStep The time step
         * @return The fraction of agents of the overall number of encountered agents at this location that was visibly symptomatic
         */
        public double getLastDayFractionSymptomatic(long timeStep) {
            if(this.locationHistory.containsKey(timeStep) && this.locationHistory.get(timeStep).getN_contacts() > 0) {
                return (double) this.locationHistory.get(timeStep).getN_symptomatic() / this.locationHistory.get(timeStep).getN_contacts();
            } else {
                return 0;
            }
        }

        /**
         * Get the average fraction of agents that were visibly symptomatic at this location in the specified
         * range of time steps
         *
         * Visible symptoms can only occur in this simulation if the observed agent is actually infected.
         *
         * @param lastTimeStep  The time step (inclusive) that ends the range
         * @param n         The time step (inclusive) that starts the range
         * @return          Average number of agents that were visibly symptomatic at this location in the specified range of time steps
         */
        public double getLastDaysFractionSymptomatic(long lastTimeStep, int n) {
            Supplier<DoubleStream> s = getFractionOfSeenStream(lastTimeStep, n, this::getLastDayFractionSymptomatic);
            return s.get().count() > 0 ? s.get().reduce(Double::sum).orElse(0d) / s.get().count() : 0;
        }

        /*
         * ==================================
         *          Helpers
         * ==================================
         */

        private Supplier<IntStream> getCountForDaysStream(long lastTimeStep, int n, LongToIntFunction f) {
            return () -> LongStream.rangeClosed(lastTimeStep - n, lastTimeStep)
                    .filter(this.locationHistory::containsKey)
                    .mapToInt(f);
        }

        private Supplier<DoubleStream> getFractionOfSeenStream(long lastTimeStep, int n, LongToDoubleFunction f) {
            return () -> LongStream.rangeClosed(lastTimeStep - n, lastTimeStep)
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

        /**
         * Construct a new visit object, which encodes one visit of one agent to one specific location
         * @param personID  PersonID
         * @param locationID    LocationID
         * @param infectionProbability Infection Probability
         * @param n_contacts    Number of contacts encountered during the visit
         * @param n_symptomatic Number of visibly symptomatic contacts encountered during the visit
         * @param n_mask        Number of people seen wearing a mask during the visit
         * @param n_distancing  Number of people seen practicing physical distancing during the visit
         */
        public Visit(long personID, long locationID, double infectionProbability, int n_contacts, int n_symptomatic, int n_mask, int n_distancing) {
            this.personID = personID;
            this.locationID = locationID;
            this.infectionProbability = infectionProbability;
            if (n_contacts < 0) {
                LOGGER.severe(String.format(
                                "Registered %d contacts for agent %d, which is negative, so setting to 0",
                                n_contacts, personID
                        )
                );
                n_contacts = 0;
            }
            this.n_contacts = n_contacts;
            if(n_mask < 0) {
                LOGGER.severe(String.format(
                                "Registered %d contacts with masks for agent %d, which is negative, so setting to 0",
                                n_mask, personID
                        )
                );
                n_mask = 0;
            }
            this.n_mask = n_mask;
            if (n_distancing < 0) {
                LOGGER.severe(String.format(
                        "Registered %d contacts who were distancing for agent %d, which is negative, so setting to 0",
                        n_distancing, personID
                    )
                );
                n_distancing = 0;
            }
            this.n_distancing = n_distancing;
            if (n_symptomatic < 0) {
                LOGGER.severe(String.format(
                        "Registered %d contacts who were coughing for agent %d, which is negative, so setting to 0",
                        n_symptomatic, personID
                    )
                );
                n_symptomatic = 0;
            }
            this.n_symptomatic = n_symptomatic;
        }

        /**
         *
         * @return PersonID
         */
        public long getPersonID() {
            return personID;
        }

        /**
         *
         * @return LocationID
         */
        public long getLocationID() {
            return locationID;
        }

        /**
         *
         * @return Infection probability
         */
        public double getInfectionProbability() {
            return infectionProbability;
        }

        /**
         *
         * @return Number of contacts encountered during this specific visit
         */
        public int getN_contacts() {
            return n_contacts;
        }

        /**
         *
         * @return Number of contacts seen wearing a mask during this visit
         */
        public int getN_mask() {
            return n_mask;
        }

        /**
         *
         * @return Number of contacts seen practicing physical distancing during this visit
         */
        public int getN_distancing() {
            return n_distancing;
        }

        /**
         *
         * @return Number of contacts with visible symptoms encountered during this visit
         */
        public int getN_symptomatic() {
            return n_symptomatic;
        }
    }
}
