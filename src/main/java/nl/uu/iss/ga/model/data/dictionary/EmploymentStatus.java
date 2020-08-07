package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum EmploymentStatus implements StringCodeTypeInterface {

    NOT_AVAILABLE("B"), // LESS_THAN_16_YEARS_OLD
    CIVILIAN_EMPLOYED_AT_WORK("1"),
    CIVILIAN_EMPLOYED_WITH_A_JOB_BUT_NOT_AT_WORK("2"),
    UNEMPLOYED("3"),
    ARMED_FORCES_AT_WORK("4"),
    ARMED_FORCES_WITH_A_JOB_BUT_NOT_AT_WORK("5"),
    NOT_IN_LABOR_FORCE("6");

    private int code;
    private final String stringCode;

    EmploymentStatus(String code) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(code);
    }

    public int getCode() {
        return code;
    }

    public String getStringCode() {
        return this.stringCode;
    }
}
