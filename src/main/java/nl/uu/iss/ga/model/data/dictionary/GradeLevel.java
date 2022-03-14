package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum GradeLevel implements StringCodeTypeInterface {

    NOT_ATTENDING("bb", false, false),
    NURSERY_SCHOOL_OR_PRESCHOOL("1", false, false),
    KINDERGARTEN("2", true, false),
    GRADE_1("3", true, false),
    GRADE_2("4", true, false),
    GRADE_3("5", true, false),
    GRADE_4("6", true, false),
    GRADE_5("7", true, false),
    GRADE_6("8", true, false),
    GRADE_7("9", true, false),
    GRADE_8("10", true, false),
    GRADE_9("11", true, false),
    GRADE_10("12", true, false),
    GRADE_11("13", false, true),
    GRADE_12("14", false, true),
    UNDERGRADUATE("15", false, true),
    GRADUATE_OR_PROFESSIONAL_BEYOND_BACHELOR("16", false, true);

    private final int code;
    private final String stringCode;

    private final boolean isK12;
    private final boolean isHigher;

    GradeLevel(String code, boolean isK12, boolean isHigher) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(code);
        this.isK12 = isK12;
        this.isHigher = isHigher;
    }

    public int getCode() {
        return code;
    }

    public String getStringCode() {
        return this.stringCode;
    }

    public boolean isK12() {
        return isK12;
    }

    public boolean isHigher() {
        return isHigher;
    }
}
