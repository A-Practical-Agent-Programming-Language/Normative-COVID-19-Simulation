package main.java.nl.uu.iss.ga;

import main.java.nl.uu.iss.ga.model.data.ActivitySchedule;
import main.java.nl.uu.iss.ga.model.reader.ActivityFileReader;
import main.java.nl.uu.iss.ga.model.reader.HouseholdReader;
import main.java.nl.uu.iss.ga.model.reader.PersonReader;
import main.java.nl.uu.iss.ga.util.ArgParse;

public class Simulation {

    public static void main(String[] args) {
        ArgParse parser = new ArgParse(args);

        System.out.println(parser.getActivityFile().getAbsoluteFile());

        HouseholdReader hr = new HouseholdReader(parser.getHouseholdsFile());
        PersonReader pr = new PersonReader(parser.getPersonsFile(), hr.getHouseholds());
        ActivityFileReader ar = new ActivityFileReader(parser.getActivityFile(), pr.getPersons());
        for(ActivitySchedule schedule : ar.getActivitySchedules()) {
            System.out.println(schedule.getPerson());
        }
    }

}
