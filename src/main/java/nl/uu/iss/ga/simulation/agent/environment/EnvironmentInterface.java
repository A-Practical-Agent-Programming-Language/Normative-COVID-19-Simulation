package main.java.nl.uu.iss.ga.simulation.agent.environment;

import main.java.nl.uu.iss.ga.model.data.dictionary.DayOfWeek;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickHookProcessor;

import java.util.*;
import java.util.stream.Collectors;

public class EnvironmentInterface implements TickHookProcessor {

    long currentTick = 0;
    DayOfWeek today = DayOfWeek.SUNDAY;

    private Map<Integer, LocationEntry> locationEntryMap;

    public long getCurrentTick() {
        return currentTick;
    }

    public DayOfWeek getToday() {
        return today;
    }

    public EnvironmentInterface(Map<Integer, LocationEntry> locationEntryMap) {
        this.locationEntryMap = locationEntryMap;
    }

    @Override
    public void tickPreHook(long l) {
        this.currentTick = l;
        this.today = CodeTypeInterface.parseAsEnum(DayOfWeek.class, (int)(currentTick % 7 + 1));
    }

    @Override
    public void tickPostHook(long l, int i, HashMap<AgentID, List<String>> hashMap) {
//        System.out.printf("Day %d. %d agents have actions", l, hashMap.size());
//        for(AgentID aid : hashMap.keySet()) {
//            System.out.printf("\t%s has %d activities: %s\n", aid.getName().getUserInfo(), hashMap.get(aid).size(), hashMap.get(aid).toString());
//        }
//        System.out.println("\n\n");

//        calculateTickRadia(hashMap);

        calculateAverageTickRadius(hashMap);
    }

    private void calculateAverageTickRadius(HashMap<AgentID, List<String>> hashMap) {
        List<Double> radia = calculateTickRadia(hashMap);
        OptionalDouble average = radia.stream().mapToDouble(x -> x).average();
        if(average.isPresent()) {
            System.out.printf("%d;%f\n", this.currentTick, average.getAsDouble());
        }
    }

    // TODO most radia are 900km+. I do not think this is correct
    private List<Double> calculateTickRadia(HashMap<AgentID, List<String>> hashMap) {
        List<Double> radia = new ArrayList<>();
        for(AgentID aid : hashMap.keySet()) {
            List<LocationEntry> locations = getLocationsForAgentForTick(hashMap.get(aid));
            OptionalDouble centroidLatitude = locations.stream().map(LocationEntry::getLatitude).mapToDouble(a -> a).average();
            OptionalDouble centroidLongitude = locations.stream().map(LocationEntry::getLongitude).mapToDouble(a -> a).average();
            double sqsum = 0.0;

            if(!(centroidLatitude.isPresent() && centroidLongitude.isPresent())) {
                System.err.println("Non centroid latitude of longitude found for agent " + aid.getShortLocalName());
                continue;
            }

            for(LocationEntry entry : locations) {
                double distance = this.haversine(centroidLatitude.getAsDouble(), centroidLongitude.getAsDouble(), entry);
                sqsum += (distance * distance);
            }
            sqsum /= locations.size();
            double radius = Math.sqrt(sqsum);
//            System.out.println(String.format(
//                    "%s,%f,%f,%d,%f",
//                    aid.getShortLocalName(),
//                    centroidLatitude.getAsDouble(),
//                    centroidLongitude.getAsDouble(),
//                    locations.size(),
//                    radius
//            ));
            radia.add(radius);
        }
        return radia;
    }

    /**
     * Extract the locationEntry objects from the string encoding an action.
     *
     * // TODO this may need updating if the return type of an activity plan changes
     * @param actions   List of actions of an agent produced during this tick
     * @return          List of locations visited by the agents during this tick
     */
    private List<LocationEntry> getLocationsForAgentForTick(List<String> actions) {
        return actions.stream()
                .map(x -> this.locationEntryMap.get(ParserUtil.parseAsInt(x.split(";")[2]))).
                filter(x -> !(x.getLongitude() == 0.0 || x.getLatitude() == 0.0))
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
