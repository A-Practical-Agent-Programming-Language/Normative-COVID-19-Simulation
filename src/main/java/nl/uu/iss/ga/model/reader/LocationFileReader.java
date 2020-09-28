package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;

public class LocationFileReader {

    private List<File> locationFiles;
    private Map<Long, Map<Integer, LocationEntry>> locations;
    private Map<Long, LocationEntry> locationsByIDMap;

    public LocationFileReader(List<File> locationFiles) {
        this.locationFiles = locationFiles;

        this.locations = new HashMap<>();
        this.locationsByIDMap = new HashMap<>();

        for(File f : this.locationFiles) {
            readLocations(f);
        }
    }

    public Map<Long, Map<Integer, LocationEntry>> getLocations() {
        return this.locations;
    }

    public Map<Long, LocationEntry> getLocationsByIDMap() {
        return locationsByIDMap;
    }

    private void readLocations(File locationFile) {
        try(
                FileInputStream is = new FileInputStream(locationFile);
                Scanner s = new Scanner(is);
        ) {
            iterateLocations(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void iterateLocations(Scanner s) {
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
