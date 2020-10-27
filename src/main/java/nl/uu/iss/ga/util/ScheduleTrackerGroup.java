package main.java.nl.uu.iss.ga.util;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

class ScheduleTrackerGroup {
    private static final Logger LOGGER = Logger.getLogger(ScheduleTrackerGroup.class.getName());

    private final File fout;
    private final List<String> headers;

    public ScheduleTrackerGroup(String parentDir, String filename, List<String> headers, String... nonAutomaticStartingHeaders) {
        this.fout = Paths.get(parentDir, filename).toFile();
        this.headers = new ArrayList<>(headers);
        createFile();
        writeLineToFile("Date;");
        for (String start : nonAutomaticStartingHeaders) {
            writeLineToFile(start + ";");
        }
        writeValuesToFile(this.headers);
    }

    public List<String> getHeaders() {
        return this.headers;
    }

    public void writeKeyMapToFile(LocalDate simulationDay, Map<String, String> map) {
        List<String> orderedValues = new ArrayList<>();
        orderedValues.add(simulationDay.format(DateTimeFormatter.ISO_DATE));
        for (String header : this.headers) {
            orderedValues.add(map.getOrDefault(header, ""));
        }
        writeValuesToFile(orderedValues);
    }

    public void writeValuesToFile(List<String> values) {
        writeLineToFile(String.join(";", values) + System.lineSeparator());
    }

    private void createFile() {
        try {
            if (!(this.fout.getParentFile().exists() || this.fout.getParentFile().mkdirs())) {
                throw new IOException("Failed to create parent directories for file " + fout.getAbsolutePath());
            }
            if (!(this.fout.exists() || this.fout.createNewFile()))
                throw new IOException("Failed to create file " + this.fout.getAbsolutePath());
            LOGGER.log(Level.INFO, "Created file " + this.fout.getAbsolutePath());
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to create schedule tracker file " + this.fout.getAbsolutePath());
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    private void writeLineToFile(String line) {
        try (
                FileOutputStream fos = new FileOutputStream(this.fout, true);
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(fos));
        ) {
            bw.write(line);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, String.format(
                    "Failed to write line for line %s to file %s",
                    line,
                    this.fout.getAbsolutePath()
            ));
        }
    }
}
