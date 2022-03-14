package nl.uu.iss.ga.model.reader;

import nl.uu.iss.ga.model.data.dictionary.LocationEntry;
import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;

public class LocationDesignationFileReader {
    private static final Logger LOGGER = Logger.getLogger(LocationDesignationFileReader.class.getName());

    private List<File> locationDesignationFiles;
    private Map<Long, LocationEntry> locations;

    public LocationDesignationFileReader(List<File> locationDesignationFiles, Map<Long, LocationEntry> locations) {
        this.locationDesignationFiles = locationDesignationFiles;
        this.locations = locations;

        for(File f : this.locationDesignationFiles) {
            readLocationDesignations(f);
        }
    }

    private void readLocationDesignations(File locationDesignationFile) {
        LOGGER.log(Level.INFO, "Reading location designation file " + locationDesignationFile.toString());
        try (
            FileInputStream is = new FileInputStream(locationDesignationFile);
            Scanner s = new Scanner(is)
        ) {
            iterateLocationDesignations(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read location designation file " + locationDesignationFile.toString(), e);
        }
    }

    private void iterateLocationDesignations(Scanner s) {
        String header = s.nextLine();
        String[] headerIndices = header.split(ParserUtil.SPLIT_CHAR);
        while(s.hasNextLine()) {
            Map<String, String> locationDesignationEntry = ParserUtil.zipLine(headerIndices, s.nextLine());
            long lid = ParserUtil.parseAsLong(locationDesignationEntry.get("lid"));
            String designation = locationDesignationEntry.get("designation");
            boolean isResidential = ParserUtil.parseIntAsBoolean(locationDesignationEntry.get("isResidential"));
            this.locations.get(lid).setDesignation(designation, isResidential);
        }
    }

}
