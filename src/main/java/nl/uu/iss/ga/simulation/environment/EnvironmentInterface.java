package main.java.nl.uu.iss.ga.simulation.environment;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.disease.DiseaseState;
import main.java.nl.uu.iss.ga.model.disease.RiskMitigationPolicy;
import main.java.nl.uu.iss.ga.model.norm.*;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHome;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.OfficesClosed;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.ReduceGatherings;
import main.java.nl.uu.iss.ga.model.norm.regimented.SchoolsClosed;
import main.java.nl.uu.iss.ga.model.norm.regimented.TakeawayOnly;
import main.java.nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.WearMaskNorm;
import main.java.nl.uu.iss.ga.simulation.agent.trigger.NewNormTrigger;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.platform.Platform;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class EnvironmentInterface implements TickHookProcessor<CandidateActivity> {

    private static final int INITIATE_NORMS = 3; // TODO too hardcoded. Should be per norm
    private final AgentStateMap agentStateMap;

    private Platform platform;

    long currentTick = 0;
    DayOfWeek today = DayOfWeek.MONDAY;

    private Map<Long, LocationEntry> locationEntryMap;

    public long getCurrentTick() {
        return currentTick;
    }

    public DayOfWeek getToday() {
        return today;
    }

    public EnvironmentInterface(Platform platform, AgentStateMap agentStateMap, Map<Long, LocationEntry> locationEntryMap) {
        this.locationEntryMap = locationEntryMap;
        this.platform = platform;
        this.agentStateMap = agentStateMap;
    }

    @Override
    public void tickPreHook(long tick) {
        this.currentTick = tick;
        this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int)(currentTick % 7 + 1));
        if(this.currentTick == INITIATE_NORMS) {
            Norm maskNorm = new WearMaskNorm();
            Norm distanceNorm = new MaintainDistanceNorm();
            Norm schoolsClosed = new SchoolsClosed();
            Norm officesClosed = new OfficesClosed();
            Norm smallGroupsNorm = new KeepGroupsSmallNorm();
            Norm restaurantsClosed = new TakeawayOnly();
            Norm reduceGatherings = new ReduceGatherings();
            Norm ifSickStayHome = new IfSickStayHome();
            this.platform.getAgents().values().forEach(x -> {
                x.addExternalTrigger(new NewNormTrigger(maskNorm));
                x.addExternalTrigger(new NewNormTrigger(distanceNorm));
                x.addExternalTrigger(new NewNormTrigger(schoolsClosed));
                x.addExternalTrigger(new NewNormTrigger(officesClosed));
                x.addExternalTrigger(new NewNormTrigger(restaurantsClosed));
                x.addExternalTrigger(new NewNormTrigger(smallGroupsNorm));
                x.addExternalTrigger(new NewNormTrigger(ifSickStayHome));
                x.addExternalTrigger(new NewNormTrigger(reduceGatherings));
            });
        }
    }

    @Override
    public void tickPostHook(long tick, int lastTickDuration, HashMap<AgentID, List<CandidateActivity>> hashMap) {
//        System.out.printf("Tick %d took %d milliseconds for %d agents (roughly %fms per agent)%n", tick, lastTickDuration, hashMap.size(), (double)lastTickDuration / hashMap.size());
        long startCalculate = System.currentTimeMillis();
        calculateAverageTickRadius(hashMap);
//        System.out.printf("Calculated radius of gyration in %d milliseconds%n", System.currentTimeMillis() - startCalculate);
        startCalculate = System.currentTimeMillis();
        storeLocationData(hashMap);
//        System.out.printf("Stored locations in %d milliseconds%n", System.currentTimeMillis() - startCalculate);
    }

    // TODO simulate agent visit output
    private void storeLocationData(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        for(List<CandidateActivity> actions : hashMap.values()) {
            for(CandidateActivity action : actions) {
                long locationID = action.getActivity().getLocation().getLocationID();
                RiskMitigationPolicy p = action.getRiskMitigationPolicy();
                DiseaseState state = action.getDiseaseState();

                if(this.locationHistory.containsKey(locationID)) {
                    this.locationHistory.get(locationID).setNewTick(this.currentTick);
                } else {
                    this.locationHistory.put(locationID, new LocationHistory(this.currentTick));
                }
                this.locationHistory.get(locationID).setVisited(p.isMask(), p.isDistance(), state.equals(DiseaseState.INFECTED_SYMPTOMATIC));
            }
        }
    }

    private ConcurrentHashMap<Long, LocationHistory> locationHistory = new ConcurrentHashMap<>();

    public LocationHistory getLocationHistory(long locationID) {
        if(this.locationHistory.containsKey(locationID)) {
            return this.locationHistory.get(locationID);
        } else {
            return new LocationHistory(currentTick);
        }
    }

    public class LocationHistory {
        ConcurrentHashMap<Long, Integer> visited = new ConcurrentHashMap<>();
        ConcurrentHashMap<Long, Integer> masks = new ConcurrentHashMap<>();
        ConcurrentHashMap<Long, Integer> distance = new ConcurrentHashMap<>();
        ConcurrentHashMap<Long, Integer> symptomatic = new ConcurrentHashMap<>();

        private long lastTick = -1;

        public LocationHistory(long lastTick) {
            setNewTick(lastTick);
        }

        private void setNewTick(long tick) {
            if(tick > this.lastTick) {
                this.lastTick = tick;
                this.visited.put(tick, 0);
                this.masks.put(tick, 0);
                this.distance.put(tick, 0);
                this.symptomatic.put(tick, 0);
            }
        }

        public void setVisited(boolean mask, boolean distance, boolean symptomatic) {
            this.visited.put(this.lastTick, this.visited.get(this.lastTick) + 1);
            if(mask)
                this.masks.put(this.lastTick, this.masks.get(this.lastTick) + 1);
            if(distance)
                this.distance.put(this.lastTick, this.distance.get(this.lastTick) + 1);
            if(symptomatic)
                this.symptomatic.put(this.lastTick, this.symptomatic.get(this.lastTick) + 1);
        }

        public int getVisitedLastDay() {
            return this.visited.get(this.lastTick);
        }

        public int getMasksLastDay() {
            return this.masks.get(this.lastTick);
        }

        public int getDistanceLastDay() {
            return this.distance.get(this.lastTick);
        }

        public int getSymptomaticLastDay() {
            return this.symptomatic.get(this.lastTick);
        }

        public int getVisitedLastNDays(long n) {
            return getLastNDayValues(this.visited, n);
        }

        public int getMaskLastNDays(long n) {
            return getLastNDayValues(this.masks, n);
        }

        public int getDistanceLastNDays(long n) {
            return getLastNDayValues(this.distance, n);
        }

        public int getSymptomaticLastNDays(long n) {
            return getLastNDayValues(this.symptomatic, n);
        }

        private int getLastNDayValues(ConcurrentHashMap<Long,Integer> fromMap, long n) {
            int value = 0;
            for(long i = this.lastTick - n; i < this.lastTick; i++) {
                if(fromMap.containsKey(i)) {
                    value += fromMap.get(i);
                }
            }
            return value;
        }
    }

    private void calculateAverageTickRadius(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        List<Double> radia = calculateTickRadia(hashMap);
        OptionalDouble average = radia.stream().mapToDouble(x -> x).average();
        if(average.isPresent()) {
            System.out.printf("%d;%f\n", this.currentTick, average.getAsDouble());
        }
    }

    // TODO most radia are 900km+. I do not think this is correct
    private List<Double> calculateTickRadia(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        List<Double> radia = new ArrayList<>();
        for(AgentID aid : hashMap.keySet()) {
            List<LocationEntry> locations = getLocationsForAgentForTick(hashMap.get(aid));
            OptionalDouble centroidLatitude = locations.stream().map(LocationEntry::getLatitude).mapToDouble(a -> a).average();
            OptionalDouble centroidLongitude = locations.stream().map(LocationEntry::getLongitude).mapToDouble(a -> a).average();
            double sqsum = 0.0;

            if(!(centroidLatitude.isPresent() && centroidLongitude.isPresent())) {
//                System.err.println("Non centroid latitude of longitude found for agent " + aid.getShortLocalName());
                continue;
            }

            for(LocationEntry entry : locations) {
                double distance = this.haversine(centroidLatitude.getAsDouble(), centroidLongitude.getAsDouble(), entry);
                sqsum += (distance * distance);
            }
            sqsum /= locations.size();
            double radius = Math.sqrt(sqsum);
            radia.add(radius);
        }
        if(radia.size() < hashMap.size())
            System.err.printf("only %d agents of %d (%f%%) produced actions%n",
                    radia.size(), hashMap.size(), radia.size() / (double) hashMap.size());
        return radia;
    }

    /**
     * Extract the locationEntry objects from the string encoding an action.
     *
     * // TODO this may need updating if the return type of an activity plan changes
     * @param actions   List of actions of an agent produced during this tick
     * @return          List of locations visited by the agents during this tick
     */
    private List<LocationEntry> getLocationsForAgentForTick(List<CandidateActivity> actions) {
        return actions.stream()
                .map(x -> x.getActivity().getLocation())
                .filter(x -> !(x.getLongitude() == 0d && x.getLatitude() == 0d))
                .collect(Collectors.toList());
    }

    @Override
    public void simulationFinishedHook(long l, int i) {
        System.exit(0);
    }

    /**
     * This method calculates the Haversine distance between two coordinates on earth.
     *
     * This method copies the implementation of a Python script with the same functionality exactly. This is important,
     * as results will be compared later. Please do not change this script lightly.
     *
     * The Python script from which this code was adapted, was based on
     * https://stackoverflow.com/questions/4913349/haversine-formula-in-python-bearing-and-distance-between-two-gps-points
     * @param centroidLatitude   Latitude of centroid of activities
     * @param centroidLongitude  Longitude of centroid of activities
     * @param location                Location entry to compare to centroid
     * @return
     */
    private double haversine(double centroidLatitude, double centroidLongitude, LocationEntry location) {
        // Convert decimal degrees to radians
        centroidLatitude = Math.toRadians(centroidLatitude);
        centroidLongitude = Math.toRadians(centroidLongitude);
        double latitude = Math.toRadians(location.getLatitude());
        double longitude = Math.toRadians(location.getLongitude());

        // Haversine formula
        double deltaLongitude = longitude - centroidLongitude;
        double deltaLatitude = latitude - centroidLatitude;

        double a = Math.pow(Math.sin(deltaLatitude/2),2) +
                Math.cos(centroidLatitude) * Math.cos(latitude) *
                        Math.pow(Math.sin(deltaLongitude/2),2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6372.8; // Radius of the earth in km. Use 3959.87433 for miles.
        return c * r;
    }
}
