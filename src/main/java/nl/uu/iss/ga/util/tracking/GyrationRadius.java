package main.java.nl.uu.iss.ga.util.tracking;

import main.java.nl.uu.iss.ga.model.data.CandidateActivity;
import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.util.Methods;
import main.java.nl.uu.iss.ga.util.config.ArgParse;
import main.java.nl.uu.iss.ga.util.config.ConfigModel;
import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.cs.iss.ga.sim2apl.core.deliberation.DeliberationResult;
import nl.uu.cs.iss.ga.sim2apl.core.tick.TickExecutor;
import org.javatuples.Pair;

import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.time.format.DateTimeFormatter.ISO_DATE;

public class GyrationRadius {
    private File fout;
    private final LocalDate simulationStartDate;
    private final ArgParse arguments;
    private final TickExecutor<CandidateActivity> executor;
    private static final Logger LOGGER = Logger.getLogger(GyrationRadius.class.getName());
    private final List<ConfigModel> counties;
    private final Map<String, Integer> numAgentsPerCounty = new HashMap<>();

    public GyrationRadius(TickExecutor<CandidateActivity> executor, ArgParse arguments, LocalDate simulationStartDate) {
        this.executor = executor;
        this.arguments = arguments;
        this.counties = arguments.getCounties();
        for(ConfigModel county : this.counties) {
            this.numAgentsPerCounty.put(county.getName(), county.getPersonReader().getPersons().size());
        }
        this.simulationStartDate = simulationStartDate;
        this.fout = new File(arguments.getOutputDir(), "tick-averages.csv");
        if(!Methods.createOutputFile(this.fout)) {
            System.exit(65);
        }
    }

    public void processSimulationDay(long tick, List<Future<DeliberationResult<CandidateActivity>>> agentActions) {
        LocalDate simulationDay = this.simulationStartDate.plusDays(tick);

        HashMap<String, Pair<Integer, Double>> perCountyRadii = new HashMap<>();
        for (ConfigModel county : this.counties) {
            double averageRadiusOfGyration = calculateAverageRadiusOfGyration(county, agentActions);
            perCountyRadii.put(county.getName(), new Pair<>(county.getFipsCode(), averageRadiusOfGyration));
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

    /**
     * Calculates the average radius of gyration over all agents in a county
     * @param county            County configuration
     * @param agentActions      Agent actions produced in the last time step
     * @return                  Average radius of gyration of agents in the county
     */
    private double calculateAverageRadiusOfGyration(ConfigModel county, List<Future<DeliberationResult<CandidateActivity>>> agentActions) {
        List<RadiusGyrationCalculator> callables = new ArrayList<>(this.arguments.getThreads());

        double total = 0; int counted = 0;
        for(int i = 0; i < arguments.getThreads(); i++) {
            callables.add(new RadiusGyrationCalculator(agentActions, county.getAgents(), i, arguments.getThreads()));
        }
        try {
            List<Future<RadiusSubResult>> futures = this.executor.useExecutorForTasks(callables);

            for(Future<RadiusSubResult> future : futures) {
                total += future.get().total;
                counted += future.get().counted;
            }
        } catch (InterruptedException | ExecutionException e) {
            e.printStackTrace();
        }

        if (counted < county.getAgents().size())
            LOGGER.log(Level.WARNING, String.format(
                    "only %d %s agents of %d (%f%%) produced actions",
                    counted, county.getName(), county.getAgents().size(), counted / (double) county.getAgents().size()));

        return total / counted;
    }

    private static Double calculateRadiusOfGyrationForAgent(List<CandidateActivity> activities) {
        List<LocationEntry> locations = getLocationsForAgentForTick(activities);
        OptionalDouble centroidLatitude = locations.stream().map(LocationEntry::getLatitude).mapToDouble(a -> a).average();
        OptionalDouble centroidLongitude = locations.stream().map(LocationEntry::getLongitude).mapToDouble(a -> a).average();
        double sqsum = 0.0;

        if (!(centroidLatitude.isPresent() && centroidLongitude.isPresent())) {
            return null;
        }

        for (LocationEntry entry : locations) {
            double distance = haversine(centroidLatitude.getAsDouble(), centroidLongitude.getAsDouble(), entry);
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
    private static List<LocationEntry> getLocationsForAgentForTick(List<CandidateActivity> actions) {
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
    private static double haversine(double centroidLatitude, double centroidLongitude, LocationEntry location) {
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

    private static class RadiusGyrationCalculator implements Callable<RadiusSubResult> {

        private final List<Future<DeliberationResult<CandidateActivity>>> agentActions;
        private final Set<AgentID> countyAgents;
        private final int thread;
        private final int threads_total;

        public RadiusGyrationCalculator(
                List<Future<DeliberationResult<CandidateActivity>>> agentActions,
                List<AgentID> countyAgents,
                int thread,
                int threads_total
        ) {
            this.agentActions = agentActions;
            this.countyAgents = new HashSet<>(countyAgents);
            this.thread = thread;
            this.threads_total = threads_total;
        }

        @Override
        public RadiusSubResult call() throws Exception {
            int start = this.thread * this.agentActions.size() / this.threads_total;
            int end = (thread + 1) * this.agentActions.size() / this.threads_total;
            if (end > this.agentActions.size()) end = this.agentActions.size();

            double total = 0; int counted = 0;
            for(int i = start; i < end; i++) {
                DeliberationResult<CandidateActivity> result = this.agentActions.get(i).get();
                if(this.countyAgents.contains(result.getAgentID())) {
                    Double radius = GyrationRadius.calculateRadiusOfGyrationForAgent(result.getActions());
                    if (radius != null) {
                        total += radius;
                        counted += 1;
                    }
                }
            }

            return new RadiusSubResult(total, counted);
        }
    }

    private static class RadiusSubResult {
        private double total;
        private int counted;

        public RadiusSubResult(double total, int counted) {
            this.total = total;
            this.counted = counted;
        }

        public double getTotal() {
            return total;
        }

        public int getCounted() {
            return counted;
        }
    }
}
