package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum SchoolEnrollment implements StringCodeTypeInterface {

    NA("b"), // (less than years("3"), old)
    NO("1"),
    PUBLIC("2"),
    PRIVATE_OR_HOMESCHOOLED("3");

    private final int code;
    private final String stringCode;

    SchoolEnrollment(String code) {
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
