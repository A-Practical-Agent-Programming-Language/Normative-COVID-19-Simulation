package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class LocationFileReader {

    private File locationFile;
    private Map<Integer, Map<Integer, LocationEntry>> locations;
    private Map<Integer, LocationEntry> locationsByIDMap;

    public LocationFileReader(File locationFile) {
        this.locationFile = locationFile;
        readLocations();
    }

    public Map<Integer, Map<Integer, LocationEntry>> getLocations() {
        return this.locations;
    }

    public Map<Integer, LocationEntry> getLocationsByIDMap() {
        return locationsByIDMap;
    }

    private void readLocations() {
        try(
                FileInputStream is = new FileInputStream(this.locationFile);
                Scanner s = new Scanner(is);
        ) {
            iterateLocations(s);
        } catch (IOException e) {
            e.printStackTrace();
            if(this.locationsByIDMap == null) {
                this.locationsByIDMap = new HashMap<>();
            }
            if(this.locations == null) {
                this.locations = new HashMap<>();
            }
        }
    }

    private void iterateLocations(Scanner s) {
        this.locations = new HashMap<>();
        this.locationsByIDMap = new HashMap<>();

        s.nextLine(); // Skip header
        while(s.hasNextLine()) {
            LocationEntry e = LocationEntry.fromLine(s.nextLine());
            if(!this.locations.containsKey(e.getPid())) {
                this.locations.put(e.getPid(), new TreeMap<>());
            }
            this.locations.get(e.getPid()).put(e.getActivity_number(), e);
            this.locationsByIDMap.put(e.getLocationID(), e);
        }
    }
}
