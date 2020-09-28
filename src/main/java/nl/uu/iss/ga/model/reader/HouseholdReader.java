package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Household;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class HouseholdReader {

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
            e.printStackTrace();
        }
        return new TreeMap<>();
    }

    private Map<Long, Household> iterateHouseholds(Scanner s) {
        Map<Long, Household> householdMap = new TreeMap<>();
        s.nextLine(); // Skip header
        while(s.hasNextLine()) {
            Household h = Household.fromCSVLine(s.nextLine());
            householdMap.put(h.getHid(), h);
            h.setLiberal(Math.random() < this.fractionConservative);
        }
        return householdMap;
    }
}
