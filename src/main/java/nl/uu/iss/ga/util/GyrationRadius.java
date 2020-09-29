package main.java.nl.uu.iss.ga.util;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;

import java.util.*;
import java.util.stream.Collectors;

public class GyrationRadius {

    private Map<Long, Map<AgentID, Double>> radii = new TreeMap<>();

    public double calculateAverageTickRadius(long tick, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        Map<AgentID, Double> radia = calculateTickRadia(hashMap);
        this.radii.put(tick, radia);
        return radia.values().stream().mapToDouble(x -> x).average().orElse(-1d);
    }

    private Map<AgentID, Double> calculateTickRadia(HashMap<AgentID, List<CandidateActivity>> hashMap) {
        Map<AgentID, Double> radia = new HashMap<>();
        for(AgentID aid : hashMap.keySet()) {
            Double radius = calculateRadiusOfGyrationForAgent(hashMap.get(aid));
            if(radius != null) {
                radia.put(aid, radius);
            }
        }
        if(radia.size() < hashMap.size())
            System.err.printf("only %d agents of %d (%f%%) produced actions%n",
                    radia.size(), hashMap.size(), radia.size() / (double) hashMap.size());
        return radia;
    }

    private Double calculateRadiusOfGyrationForAgent(List<CandidateActivity> activities) {
        List<LocationEntry> locations = getLocationsForAgentForTick(activities);
        OptionalDouble centroidLatitude = locations.stream().map(LocationEntry::getLatitude).mapToDouble(a -> a).average();
        OptionalDouble centroidLongitude = locations.stream().map(LocationEntry::getLongitude).mapToDouble(a -> a).average();
        double sqsum = 0.0;

        if(!(centroidLatitude.isPresent() && centroidLongitude.isPresent())) {
            return null;
        }

        for(LocationEntry entry : locations) {
            double distance = this.haversine(centroidLatitude.getAsDouble(), centroidLongitude.getAsDouble(), entry);
            sqsum += (distance * distance);
        }
        sqsum /= locations.size();
        return Math.sqrt(sqsum);
    }

    /**
     * Extract the locationEntry objects from the string encoding an action.
     *
     * @param actions   List of actions of an agent produced during this tick
     * @return          List of locations visited by the agents during this tick
     */
    private List<LocationEntry> getLocationsForAgentForTick(List<CandidateActivity> actions) {
        return actions.stream()
                .map(x -> x.getActivity().getLocation())
                .filter(x -> !(x.getLongitude() == 0d && x.getLatitude() == 0d))
                .collect(Collectors.toList());
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
