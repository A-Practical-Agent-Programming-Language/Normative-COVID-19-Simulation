package main.java.nl.uu.iss.ga.model.disease;

public enum DiseaseState {
    SUSCEPTIBLE(0),
    EXPOSED(1),
    INFECTED(2),
    RECOVERED(3);

    private final int code;

    DiseaseState(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
