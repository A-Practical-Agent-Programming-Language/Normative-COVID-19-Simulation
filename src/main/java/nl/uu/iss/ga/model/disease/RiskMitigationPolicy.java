package main.java.nl.uu.iss.ga.model.disease;

public enum RiskMitigationPolicy {
    NONE(0),
    MASK(1),
    DISTANCING(2),
    BOTH(3);

    private int code;

    RiskMitigationPolicy(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
