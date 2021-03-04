package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import org.javatuples.Pair;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

public class GyrationRadius {
    private File fout;
    private final LocalDate simulationStartDate;
    private static final Logger LOGGER = Logger.getLogger(GyrationRadius.class.getName());
    private final List<ConfigModel> counties;
    private final Map<String, Integer> numAgentsPerCounty = new HashMap<>();

    public GyrationRadius(String outputDir, LocalDate simulationStartDate, List<ConfigModel> counties) {
        this.counties = counties;
        for(ConfigModel county : this.counties) {
            this.numAgentsPerCounty.put(county.getName(), county.getPersonReader().getPersons().size());
        }
        this.simulationStartDate = simulationStartDate;
        try {
            createOutFile(outputDir);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
            System.exit(65);
        }
    }

    public void processSimulationDay(long tick, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        LocalDate simulationDay = this.simulationStartDate.plusDays(tick);

        HashMap<String, Pair<Integer, Double>> perCountyRadii = new HashMap<>();
        for (ConfigModel county : this.counties) {
            Map<AgentID, Double> radia = calculateTickRadia(county, hashMap);
            perCountyRadii.put(county.getName(), new Pair<>(county.getFipsCode(), radia.values().stream().mapToDouble(x -> x).average().orElse(-1d)));
        }

        for(String fips : perCountyRadii.keySet()) {
            LOGGER.log(Level.INFO, String.format(
                    "%s %s (tick %d): [%s] %f",
                    simulationDay.getDayOfWeek().name(),
                    simulationDay.format(ISO_DATE),
                    tick,
                    fips,
                    perCountyRadii.get(fips).getValue1()));
        }

        writeAveragesToFile(perCountyRadii, simulationDay);
    }

    private void createOutFile(String outputDir) throws IOException {
        this.fout = (Path.of(outputDir).isAbsolute() ?
                Paths.get(outputDir, "tick-averages.csv") :
                Paths.get("output", outputDir, "tick-averages.csv")).toFile();

        if (!(fout.getParentFile().exists() || fout.getParentFile().mkdirs()) ||
                !(fout.exists() || fout.createNewFile())) {
            throw new IOException("Failed to create file " + fout.getAbsolutePath());
        }
        if (!(fout.exists() || fout.createNewFile())) {
            throw new IOException("Failed to create file " + fout.getName());
        }
    }

    private void writeAveragesToFile(HashMap<String, Pair<Integer, Double>> lastTickAverages, LocalDate date) {
        boolean writeHeader = !fout.exists();
        try (
                FileOutputStream fos = new FileOutputStream(fout, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos))
        ) {
            if (!(fout.exists() || fout.createNewFile())) {
                throw new IOException("Failed to create file " + fout.getName());
            }
            if (writeHeader) {
                bw.write("date,COUNTYFP,GyrationRadiusKm,#agents\n");
            }
            for (String countyname : lastTickAverages.keySet()) {
                bw.write(date.format(DateTimeFormatter.ISO_DATE));
                bw.write(",");
                bw.write(Integer.toString(lastTickAverages.get(countyname).getValue0()));
                bw.write(",");
                bw.write(Double.toString(lastTickAverages.get(countyname).getValue1()));
                bw.write(",");
                bw.write(Integer.toString(this.numAgentsPerCounty.get(countyname)));
                bw.write("\n");
            }
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to write last county averages to file.", e);
        }
    }

    private Map<AgentID, Double> calculateTickRadia(ConfigModel county, HashMap<AgentID, List<CandidateActivity>> hashMap) {
        Map<AgentID, Double> radia = new HashMap<>();
        for (AgentID aid : county.getAgents()) {
            Double radius = calculateRadiusOfGyrationForAgent(hashMap.getOrDefault(aid, Collections.emptyList()));
            if (radius != null) {
                radia.put(aid, radius);
            }
        }
        if (radia.size() < county.getAgents().size())
            LOGGER.log(Level.WARNING, String.format(
                    "only %d %s agents of %d (%f%%) produced actions",
                    radia.size(), county.getName(), county.getAgents().size(), radia.size() / (double) county.getAgents().size()));
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
