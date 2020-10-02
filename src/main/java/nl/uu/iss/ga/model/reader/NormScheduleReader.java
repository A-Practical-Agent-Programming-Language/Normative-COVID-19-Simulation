package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.norm.Norm;
import main.java.nl.uu.iss.ga.model.norm.NormContainer;
import main.java.nl.uu.iss.ga.model.norm.NormFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NormScheduleReader {

    private Map<LocalDate, List<NormContainer>> eventsMap;

    private static final Logger LOGGER = Logger.getLogger(NormScheduleReader.class.getName());

    public NormScheduleReader(File normFile) {
        readNorms(normFile);
    }

    public Map<LocalDate, List<NormContainer>> getEventsMap() {
        return eventsMap;
    }

    private void readNorms(File normFile) {
        LOGGER.log(Level.INFO, "Reading norms file " + normFile.toString());
        List<NormContainer> norms = null;
        try (
                FileInputStream is = new FileInputStream(normFile);
                Scanner s = new Scanner(is);
        ) {
            norms = iterateNorms(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read norms file " + normFile.toString(), e);
        }
        this.eventsMap = norms == null ? Collections.emptyMap() : makeEventMap(norms);
    }

    private List<NormContainer> iterateNorms(Scanner s) {
        String[] header = s.nextLine().split(ParserUtil.SPLIT_CHAR);
        List<NormContainer> normsList = new ArrayList<>();
        while(s.hasNextLine()) {
            Map<String, String> keyValue = ParserUtil.zipEscapedCSVLine(header, s.nextLine());
            Norm norm = NormFactory.fromCSVLine(keyValue);
            LocalDate start = LocalDate.parse(keyValue.get("start"), DateTimeFormatter.ISO_DATE);
            LocalDate end = null;
            if(keyValue.get("end") != null && !keyValue.get("end").isBlank()) {
                end = LocalDate.parse(keyValue.get("end"), DateTimeFormatter.ISO_DATE);
            }
            String statement = keyValue.get("statement");
            normsList.add(new NormContainer(norm, start, end, statement));
        }
        return normsList;
    }

    private Map<LocalDate, List<NormContainer>> makeEventMap(List<NormContainer> norms) {
        Map<LocalDate, List<NormContainer>> events = new TreeMap<>();
        for(NormContainer c : norms) {
            if(!events.containsKey(c.getStartDate()))
                events.put(c.getStartDate(), new ArrayList<>());
            events.get(c.getStartDate()).add(c);

            if(c.getEndDate() != null) {
                if(!events.containsKey(c.getEndDate()))
                    events.put(c.getEndDate(), new ArrayList<>());
                events.get(c.getEndDate()).add(c);
            }
        }
        return events;
    }

}
