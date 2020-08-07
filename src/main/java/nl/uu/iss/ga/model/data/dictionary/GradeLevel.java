package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum GradeLevel implements StringCodeTypeInterface {

    NOT_ATTENDING("bb"),
    NURSERY_SCHOOL_OR_PRESCHOOL("1"),
    KINDERGARTEN("2"),
    GRADE_1("3"),
    GRADE_2("4"),
    GRADE_3("5"),
    GRADE_4("6"),
    GRADE_5("7"),
    GRADE_6("8"),
    GRADE_7("9"),
    GRADE_8("10"),
    GRADE_9("11"),
    GRADE_10("12"),
    GRADE_11("13"),
    GRADE_12("14"),
    UNDERGRADUATE("15"),
    GRADUATE_OR_PROFESSIONAL_BEYOND_BACHELOR("16");

    private final int code;
    private final String stringCode;

    GradeLevel(String code) {
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
