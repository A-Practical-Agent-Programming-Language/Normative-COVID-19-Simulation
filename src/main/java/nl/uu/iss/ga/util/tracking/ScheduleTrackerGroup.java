package nl.uu.iss.ga.util.tracking;

import nl.uu.iss.ga.util.Methods;

import java.io.*;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ScheduleTrackerGroup {
    private static final Logger LOGGER = Logger.getLogger(ScheduleTrackerGroup.class.getName());

    private final String dir;
    private final File fout;
    private final List<String> headers;

    public ScheduleTrackerGroup(File parentDir, String filename, List<String> headers, String... nonAutomaticStartingHeaders) {
        this.dir = parentDir.getAbsolutePath();
        this.fout = Paths.get(this.dir, filename).toFile();
        this.headers = new ArrayList<>(headers);
        Methods.createOutputFile(this.fout);
        writeLineToFile("Date;Dir;");
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
        orderedValues.add(dir);
        for (String header : this.headers) {
            orderedValues.add(map.getOrDefault(header, ""));
        }
        writeValuesToFile(orderedValues);
    }

    public void writeValuesToFile(List<String> values) {
        writeLineToFile(String.join(";", values) + System.lineSeparator());
    }

    public void writeLineToFile(String line) {
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
