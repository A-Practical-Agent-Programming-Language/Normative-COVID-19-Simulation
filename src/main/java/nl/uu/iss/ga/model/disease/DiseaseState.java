package nl.uu.iss.ga.model.disease;

import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum DiseaseState implements CodeTypeInterface {
    NOT_SET(-1),
    SUSCEPTIBLE(0),
    EXPOSED(1),
    INFECTED_SYMPTOMATIC(2),
    INFECTED_ASYMPTOMATIC(3),
    RECOVERED(4);

    private final int code;

    DiseaseState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
