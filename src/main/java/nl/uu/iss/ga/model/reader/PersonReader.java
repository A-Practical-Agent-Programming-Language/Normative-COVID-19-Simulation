package nl.uu.iss.ga.model.reader;

import nl.uu.iss.ga.model.data.Household;
import nl.uu.iss.ga.model.data.Person;
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

public class PersonReader {

    private static final Logger LOGGER = Logger.getLogger(PersonReader.class.getName());

    private final List<File> personsFiles;
    private final Map<Long, Household> householdMap;
    private final Map<Long, Person> persons;

    public PersonReader(List<File> personsFiles, Map<Long, Household> householdMap) {
        this.personsFiles = personsFiles;
        this.householdMap = householdMap;

        this.persons = new TreeMap<>();
        for(File f : this.personsFiles) {
            this.persons.putAll(readPersons(f));
        }
    }

    public Map<Long, Person> getPersons() {
        return persons;
    }

    private Map<Long, Person> readPersons(File personsFile) {
        LOGGER.log(Level.INFO, "Reading persons file " + personsFile);
        try(
                FileInputStream is = new FileInputStream(personsFile);
                Scanner s = new Scanner(is);
        ) {
            return iteratePersons(s);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Failed to read person file " + personsFile.toString(), e);
        }
        return new TreeMap<>();
    }

    private Map<Long, Person> iteratePersons(Scanner s) {
        Map<Long, Person> personMap = new TreeMap<>();
        String[] header = s.nextLine().split(ParserUtil.SPLIT_CHAR);
        while(s.hasNextLine()) {
            Person p = Person.fromLine(this.householdMap, ParserUtil.zipLine(header, s.nextLine()));
            personMap.put(p.getPid(), p);
        }
        return personMap;
    }
}
