package main.java.nl.uu.iss.ga.model.disease;

public enum RiskMitigationPolicy {
    NONE(0, false, false),
    MASK(1, true, false),
    DISTANCING(2, false, true),
    BOTH(3, true, true);

    private final int code;
    private final boolean mask;
    private final boolean distance;

    RiskMitigationPolicy(int code, boolean mask, boolean distance) {
        this.code = code;
        this.mask = mask;
        this.distance = distance;
    }

    public int getCode() {
        return code;
    }

    public boolean isMask() {
        return mask;
    }

    public boolean isDistance() {
        return distance;
    }
}
