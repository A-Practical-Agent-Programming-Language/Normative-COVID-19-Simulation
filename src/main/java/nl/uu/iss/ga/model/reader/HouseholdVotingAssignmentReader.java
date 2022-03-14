package nl.uu.iss.ga.model.reader;

import nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class HouseholdVotingAssignmentReader {

    private static final Logger LOGGER = Logger.getLogger(HouseholdVotingAssignmentReader.class.getName());

    private final Map<Long, Boolean> householdVotingAssignment;

    public HouseholdVotingAssignmentReader(List<File> votingAssignmentFiles) {
        this.householdVotingAssignment = new TreeMap<>();
        for(File f : votingAssignmentFiles) {
            this.householdVotingAssignment.putAll(readVotingAssignments(f));
        }
    }

    public Map<Long, Boolean> getHouseholdVotingAssignment() {
        return householdVotingAssignment;
    }

    private Map<Long, Boolean> readVotingAssignments(File votingAssignmentFile) {
        LOGGER.log(Level.INFO, "Reading voting assignment file " + votingAssignmentFile.toString());
        try(
                FileInputStream is = new FileInputStream(votingAssignmentFile);
                Scanner s = new Scanner(is);
        ) {
            return iterateVotingAssignments(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read voting assignment file " + votingAssignmentFile.toString(), e);
        }
        return new TreeMap<>();
    }

    private Map<Long, Boolean> iterateVotingAssignments(Scanner s) {
        Map<Long, Boolean> votingAssignments = new TreeMap<>();
        String header = s.nextLine();
        String[] headerIndices = header.split(ParserUtil.SPLIT_CHAR);
        while (s.hasNextLine()) {
            Map<String, String> line = ParserUtil.zipLine(headerIndices, s.nextLine());
            long hid = ParserUtil.parseAsLong(line.get("hid"));
            boolean liberal = ParserUtil.parseIntAsBoolean(line.get("is_liberal"));
            votingAssignments.put(hid, liberal);
        }
        return votingAssignments;
    }

}
