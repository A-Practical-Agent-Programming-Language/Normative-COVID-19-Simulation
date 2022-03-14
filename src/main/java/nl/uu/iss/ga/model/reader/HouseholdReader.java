package nl.uu.iss.ga.model.reader;

import nl.uu.iss.ga.model.data.Household;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HouseholdReader {

    private static final Logger LOGGER = Logger.getLogger(HouseholdReader.class.getName());

    private final Map<Long, Household> households;
    private final Map<Long, Boolean> householdVotingAssignments;
    private final double fractionLiberal;
    private final Random rnd;

    public HouseholdReader(
            List<File> householdFiles,
            Map<Long, Boolean> householdVotingAssignments,
            double fractionLiberal, Random rnd
    ) {
        this.rnd = rnd;
        this.householdVotingAssignments = householdVotingAssignments;
        this.fractionLiberal = fractionLiberal;

        this.households = new TreeMap<>();
        for(File f : householdFiles) {
            this.households.putAll(readHouseholds(f));
        }
    }

    public Map<Long, Household> getHouseholds() {
        return households;
    }

    private Map<Long, Household> readHouseholds(File householdFile) {
        LOGGER.log(Level.INFO, "Reading household file " + householdFile.toString());
        try(
                FileInputStream is = new FileInputStream(householdFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateHouseholds(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read household file " + householdFile.toString(), e);
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
            h.setLiberal(this.householdVotingAssignments == null ?
                    this.rnd.nextFloat() < this.fractionLiberal :
                    this.householdVotingAssignments.get(h.getHid())
            );
        }
        return householdMap;
    }
}
