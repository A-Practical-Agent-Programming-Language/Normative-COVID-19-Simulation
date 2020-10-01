package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Household;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HouseholdReader {

    private static final Logger LOGGER = Logger.getLogger(HouseholdReader.class.getName());

    private final List<File> householdFiles;
    private final Map<Long, Household> households;
    private double fractionConservative;

    public HouseholdReader(List<File> householdFiles, double fractionConservative) {
        this.householdFiles = householdFiles;
        this.fractionConservative = fractionConservative;

        this.households = new TreeMap<>();
        for(File f : this.householdFiles) {
            this.households.putAll(readHouseholds(f));
        }
    }

    public Map<Long, Household> getHouseholds() {
        return households;
    }

    private Map<Long, Household> readHouseholds(File householdFile) {
        try(
                FileInputStream is = new FileInputStream(householdFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateHouseholds(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read houehold file " + householdFile.toString(), e);
        }
        return new TreeMap<>();
    }

    private Map<Long, Household> iterateHouseholds(Scanner s) {
        Map<Long, Household> householdMap = new TreeMap<>();
        String header = s.nextLine();
        String[] headerIndices = header.split(ParserUtil.SPLIT_CHAR);
        while(s.hasNextLine()) {
            Household h = Household.fromCSVLine(ParserUtil.zipLine(headerIndices, s.nextLine()));
            householdMap.put(h.getHid(), h);
            h.setLiberal(Math.random() < this.fractionConservative);
        }
        return householdMap;
    }
}
