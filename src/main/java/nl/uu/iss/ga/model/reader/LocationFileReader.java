package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationFileReader {

    private static final Logger LOGGER = Logger.getLogger(LocationFileReader.class.getName());

    private List<File> locationFiles;
    private Map<Long, Map<Integer, LocationEntry>> locations;
    private Map<Long, LocationEntry> locationsByIDMap;

    public LocationFileReader(List<File> locationFiles) {
        this.locationFiles = locationFiles;

        this.locations = new HashMap<>();
        this.locationsByIDMap = new HashMap<>();

        for (File f : this.locationFiles) {
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
            LOGGER.log(Level.SEVERE, "Failed to read location file " + locationFile.toString(), e);
        }
    }

    private void iterateLocations(Scanner s) {
        String header = s.nextLine();
        String[] headerIndices = header.split(ParserUtil.SPLIT_CHAR);
        while(s.hasNextLine()) {
            LocationEntry e = LocationEntry.fromLine(ParserUtil.zipLine(headerIndices, s.nextLine()));
            if(!this.locations.containsKey(e.getPid())) {
                this.locations.put(e.getPid(), new TreeMap<>());
            }
            this.locations.get(e.getPid()).put(e.getActivity_number(), e);
            this.locationsByIDMap.put(e.getLocationID(), e);
        }
    }
}
