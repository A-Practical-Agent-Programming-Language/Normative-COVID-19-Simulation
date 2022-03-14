package nl.uu.iss.ga.model.disease;

import nl.uu.iss.ga.model.data.Person;
import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum AgentGroup implements CodeTypeInterface {
    UNDER_9(0),
    TEEN(1),
    TWENTIES(2),
    THIRTIES(3),
    FORTIES(4),
    FIFTIES(5),
    SIXTIES(6),
    SEVENTIES_PLUS(7);

    private final int code;

    AgentGroup(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return this.code;
    }

    public static AgentGroup fromPerson(Person person) {
        AgentGroup group;
        if (person.getAge() < 10) {
            group = UNDER_9;
        } else if (person.getAge() < 20) {
            group = TEEN;
        } else if (person.getAge() < 30) {
            group = TWENTIES;
        } else if (person.getAge() < 40) {
            group = THIRTIES;
        } else if (person.getAge() < 50) {
            group = FORTIES;
        } else if (person.getAge() < 60) {
            group = FIFTIES;
        } else if (person.getAge() < 70) {
            group = SIXTIES;
        } else {
            group = SEVENTIES_PLUS;
        }
        return group;
    }

}
