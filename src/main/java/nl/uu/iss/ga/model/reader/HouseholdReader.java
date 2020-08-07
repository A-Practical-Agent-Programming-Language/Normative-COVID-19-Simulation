package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Household;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class HouseholdReader {

    private final File householdFile;
    private final Map<Integer, Household> households;

    public HouseholdReader(File householdFile) {
        this.householdFile = householdFile;
        this.households = readHouseholds();
    }

    public Map<Integer, Household> getHouseholds() {
        return households;
    }

    private Map<Integer, Household> readHouseholds() {
        try(
                FileInputStream is = new FileInputStream(this.householdFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateHouseholds(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TreeMap<>();
    }

    private Map<Integer, Household> iterateHouseholds(Scanner s) {
        Map<Integer, Household> householdMap = new TreeMap<>();
        s.nextLine(); // Skip header
        while(s.hasNextLine()) {
            Household h = Household.fromCSVLine(s.nextLine());
            householdMap.put(h.getHid(), h);
        }
        return householdMap;
    }
}
