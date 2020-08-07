package main.java.nl.uu.iss.ga.model.reader;

import main.java.nl.uu.iss.ga.model.data.Household;
import main.java.nl.uu.iss.ga.model.data.Person;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class PersonReader {

    private final File personsFile;
    private final Map<Integer, Household> householdMap;
    private final Map<Integer, Person> persons;

    public PersonReader(File personsFile, Map<Integer, Household> householdMap) {
        this.personsFile = personsFile;
        this.householdMap = householdMap;
        this.persons = readPersons();
    }

    public Map<Integer, Person> getPersons() {
        return persons;
    }

    private Map<Integer, Person> readPersons() {
        try(
                FileInputStream is = new FileInputStream(this.personsFile);
                Scanner s = new Scanner(is);
        ) {
            return iteratePersons(s);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new TreeMap<>();
    }

    private Map<Integer, Person> iteratePersons(Scanner s) {
        Map<Integer, Person> householdMap = new TreeMap<>();
        s.nextLine(); // Skip header
        while(s.hasNextLine()) {
            Person p = Person.fromLine(this.householdMap, s.nextLine());
            householdMap.put(p.getPid(), p);
        }
        return householdMap;
    }
}
