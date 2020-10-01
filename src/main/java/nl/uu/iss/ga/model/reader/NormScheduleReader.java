package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.norm.Norm;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NormScheduleReader {

    private File normFile;
    private Map<LocalDate, List<Norm>> eventsMap;

    private static final Logger LOGGER = Logger.getLogger(NormScheduleReader.class.getName());

    public NormScheduleReader(File normFile) {
        this.normFile = normFile;
    }

    public Map<LocalDate, List<Norm>> getEventsMap() {
        return eventsMap;
    }

    private void readNorms(File normFile) {
        try (
                FileInputStream is = new FileInputStream(normFile);
                Scanner s = new Scanner(is);
        ) {
            iterateNorms(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read norms file " + normFile.toString());
        }
    }

    private List<Norm> iterateNorms(Scanner s) {
        String[] header = s.nextLine().split(ParserUtil.SPLIT_CHAR);
        List<Norm> normsList = new ArrayList<>();
        while(s.hasNextLine()) {
            Map<String, String> keyValue = ParserUtil.zipLine(header, s.nextLine());

        }
        return normsList;
    }


}
