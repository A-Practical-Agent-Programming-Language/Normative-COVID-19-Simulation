package main.java.nl.uu.iss.ga.util;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.simulation.environment.AgentStateMap;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import org.javatuples.Pair;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class GyrationRadius {
    private static final Logger LOGGER = Logger.getLogger(GyrationRadius.class.getName());
    private final AgentStateMap agentStateMap;
    private List<ConfigModel> counties;
    private Map<Integer, Map<Long, Map<AgentID, Double>>> radii = new TreeMap<>();

    public GyrationRadius(List<ConfigModel> counties, AgentStateMap agentStateMap) {
        this.counties = counties;
        this.agentStateMap = agentStateMap;
    }

    public HashMap<String, Pair<Integer, Double>> calculateAverageTickRadius(long tick, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        HashMap<String, Pair<Integer, Double>> perCountyRadii = new HashMap<>();
        for (ConfigModel county : this.counties) {
            Map<AgentID, Double> radia = calculateTickRadia(county, hashMap);
            if (!this.radii.containsKey(county.getFipsCode())) {
                this.radii.put(county.getFipsCode(), new HashMap<>());
            }
            this.radii.get(county.getFipsCode()).put(tick, radia);
            perCountyRadii.put(county.getName(), new Pair<>(county.getFipsCode(), radia.values().stream().mapToDouble(x -> x).average().orElse(-1d)));
        }

        return perCountyRadii;
    }

    public void writeAveragesToFile(File fout, HashMap<String, Pair<Integer, Double>> lastTickAverages, LocalDate date) {
        boolean writeHeader = !fout.exists();
        try (
                FileOutputStream fos = new FileOutputStream(fout, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            if (!(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getName());
            }
            if (writeHeader) {
                bw.write("date,COUNTYFP,GyrationRadiusKm\n");
            }
            for (String countyname : lastTickAverages.keySet()) {
                bw.write(date.format(DateTimeFormatter.ISO_DATE));
                bw.write(",");
                bw.write(Integer.toString(lastTickAverages.get(countyname).getValue0()));
                bw.write(",");
                bw.write(Double.toString(lastTickAverages.get(countyname).getValue1()));
                bw.write("\n");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write last county averages to file.", e);
        }
    }

    public void writeResults(ConfigModel county, File fout, Map<AgentID, Long> agentIDpidMap, LocalDate firstDate, boolean timestepInColumn) throws IOException {
        FileOutputStream fos = new FileOutputStream(fout);
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));

        if (timestepInColumn)
            writeResultsTickInColumn(county, firstDate, bw, agentIDpidMap);
        else
            writeResultByAgent(county, firstDate, bw, agentIDpidMap);

        bw.close();
        LOGGER.log(Level.INFO, "Wrote radii of gyration to " + fout.getAbsolutePath());
    }

    private void writeResultsTickInColumn(ConfigModel county, LocalDate firstDate, BufferedWriter bw, Map<AgentID, Long> agentIDpidMap) throws IOException {
        Map<Long, Map<AgentID, Double>> localRadii = this.radii.get(county.getFipsCode());
        String[] averages = new String[localRadii.size()];
        bw.write("pid;COUNTYFP;");
        for (long i = 0; i < localRadii.size(); i++) {
            if (firstDate == null) {
                bw.write(Long.toString(i));
            } else {
                bw.write(firstDate.plusDays(i).format(DateTimeFormatter.ISO_DATE));
            }
            bw.write(";");
            averages[(int) i] = Double.toString(localRadii.get(i).values().stream().reduce(Double::sum).orElse(0d) / localRadii.get(i).size());
        }

        bw.write("\n");
        for (AgentID aid : county.getAgents()) {
            bw.write(Long.toString(agentIDpidMap.get(aid)));
            bw.write(";");
            bw.write(county.getFipsCode());
            bw.write(";");

            for (long i = 0; i < localRadii.size(); i++) {
                if (localRadii.get(i).containsKey(aid)) {
                    bw.write(Double.toString(localRadii.get(i).get(aid)));
                    bw.write(";");
                } else {
                    bw.write("-1;"); // No actions produced, so no gyration (not the same as staying home!)
                }
            }
            bw.write("\n");
        }

        bw.write("average;");
        bw.write(county.getFipsCode());
        bw.write(";");
        bw.write(String.join(";", averages));
        bw.write(";");
    }

    private void writeResultByAgent(ConfigModel county, LocalDate firstDate, BufferedWriter bw, Map<AgentID, Long> agentIDpidMap) throws IOException {
        bw.write("pid;tick;COUNTYFP;"); // TODO add COUNTYFP
        if (firstDate != null) {
            bw.write("date;");
        }
        bw.write("GyrationRadiusKm\n");

        Map<Long, Map<AgentID, Double>> localRadii = this.radii.get(county.getFipsCode());

        for (long i = 0; i < this.radii.size(); i++) {
            for (AgentID aid : county.getAgents()) {
                bw.write(Long.toString(agentIDpidMap.get(aid)));
                bw.write(";");
                bw.write(Long.toString(i));
                bw.write(";");
                bw.write(this.agentStateMap.getFipsCode(aid));
                bw.write(";");
                if (firstDate != null) {
                    bw.write(firstDate.plusDays(i).format(DateTimeFormatter.ISO_DATE));
                    bw.write(";");
                }
                bw.write(Double.toString(localRadii.get(i).get(aid)));
            }
        }
    }

    private Map<AgentID, Double> calculateTickRadia(ConfigModel county, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        Map<AgentID, Double> radia = new HashMap<>();
        for (AgentID aid : county.getAgents()) {
            Double radius = calculateRadiusOfGyrationForAgent(hashMap.get(aid));
            if (radius != null) {
                radia.put(aid, radius);
            }
        }
        if (radia.size() < county.getAgents().size())
            LOGGER.log(Level.WARNING, String.format(
                    "only %d %s agents of %d (%f%%) produced actions",
                    radia.size(), county.getName(), hashMap.size(), radia.size() / (double) hashMap.size()));
        return radia;
    }

    private Double calculateRadiusOfGyrationForAgent(List<CandidateActivity> activities) {
        List<LocationEntry> locations = getLocationsForAgentForTick(activities);
        OptionalDouble centroidLatitude = locations.stream().map(LocationEntry::getLatitude).mapToDouble(a -> a).average();
        OptionalDouble centroidLongitude = locations.stream().map(LocationEntry::getLongitude).mapToDouble(a -> a).average();
        double sqsum = 0.0;

        if (!(centroidLatitude.isPresent() && centroidLongitude.isPresent())) {
            return null;
        }

        for (LocationEntry entry : locations) {
            double distance = this.haversine(centroidLatitude.getAsDouble(), centroidLongitude.getAsDouble(), entry);
            sqsum += (distance * distance);
        }
        sqsum /= locations.size();
        return Math.sqrt(sqsum);
    }

    /**
     * Extract the locationEntry objects from the string encoding an action.
     *
     * @param actions List of actions of an agent produced during this tick
     * @return List of locations visited by the agents during this tick
     */
    private List<LocationEntry> getLocationsForAgentForTick(List<CandidateActivity> actions) {
        return actions.stream()
                .map(x -> x.getActivity().getLocation())
                .filter(x -> !(x.getLongitude() == 0d && x.getLatitude() == 0d))
                .collect(Collectors.toList());
    }

    /**
     * This method calculates the Haversine distance between two coordinates on earth.
     * <p>
     * This method copies the implementation of a Python script with the same functionality exactly. This is important,
     * as results will be compared later. Please do not change this script lightly.
     * <p>
     * The Python script from which this code was adapted, was based on
     * https://stackoverflow.com/questions/4913349/haversine-formula-in-python-bearing-and-distance-between-two-gps-points
     *
     * @param centroidLatitude  Latitude of centroid of activities
     * @param centroidLongitude Longitude of centroid of activities
     * @param location          Location entry to compare to centroid
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

        double a = Math.pow(Math.sin(deltaLatitude / 2), 2) +
                Math.cos(centroidLatitude) * Math.cos(latitude) *
                        Math.pow(Math.sin(deltaLongitude / 2), 2);

        double c = 2 * Math.asin(Math.sqrt(a));
        double r = 6372.8; // Radius of the earth in km. Use 3959.87433 for miles.
        return c * r;
    }
}
