package nl.uu.iss.ga.util.tracking.activities;

import nl.uu.cs.iss.ga.sim2apl.core.agent.AgentID;
import nl.uu.iss.ga.pansim.state.AgentStateMap;
import nl.uu.iss.ga.util.Methods;
import nl.uu.iss.ga.util.config.SimulationArguments;

import java.io.*;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class logs the trust values of all individual agents.
 * Make sure to disable this feature if fast performance is required! For debugging purposes only
 */
public class TrustLogger {

    private static final Logger LOGGER = Logger.getLogger(TrustLogger.class.getSimpleName());
    private Thread thread;
    private TrustWriter trustWriter;
    private AgentStateMap agentStateMap;

    /**
     * Instantiate once per time step
     * @param timestep  Time step that is currently being processed
     */
    public TrustLogger(long timestep, AgentStateMap agentStateMap) throws IOException {
        this.trustWriter = new TrustWriter(timestep);
        this.agentStateMap = agentStateMap;
        thread = new Thread(this.trustWriter);
        thread.start();
    }

    public void processAgent(AgentID agentID, double trust) {
        trustWriter.add(String.format(
                "%d;%f\n",
                this.agentStateMap.getAgentToPidMap().get(agentID),
                trust
        ));
    }

    public void close() {
        trustWriter.close();
        try {
            thread.join();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    private static class TrustWriter implements Runnable {

        private final ConcurrentLinkedQueue<String> outputQueue = new ConcurrentLinkedQueue<>();
        private final FileWriter fileWriter;
        private final BufferedWriter writer;

        private boolean active = true;

        public TrustWriter(long timestep) throws IOException {
            File outputFile = Path.of(
                    SimulationArguments.getInstance().getOutputDir().getAbsolutePath(),
                    "trust", String.format(
                            "trust-timestep-%04d.csv", timestep)).toFile();
            Methods.createOutputFile(outputFile);
            this.fileWriter = new FileWriter(outputFile);
            this.writer = new BufferedWriter(fileWriter);
        }

        @Override
        public void run() {
            while(active || !outputQueue.isEmpty()) {
                String nextLine = outputQueue.poll();
                if (nextLine != null) {
                    try {
                        this.writer.write(nextLine);
                    } catch (IOException e) {
                        LOGGER.log(Level.SEVERE, "Failed to write trust value to file: " + nextLine, e);
                    }
                }
            }
            try {
                writer.close();
                this.fileWriter.close();
            } catch (IOException e) {
                LOGGER.severe("Failed to close output stream for trust file");
            }
        }

        public void add(String outputLine) {
            this.outputQueue.add(outputLine);
        }

        public void close() {
            active = false;
        }
    }

}
