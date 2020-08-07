package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum BusinessOnProperty implements StringCodeTypeInterface {
    NA("b"),  // (GQ/not a one-family house or mobile home)
    YES("1"),
    NO("2"),
    CASE_IS_FROM_2016_OR_LATER("9");

    private final int code;
    private final String stringCode;

    BusinessOnProperty(String code) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(code);
    }

    @Override
    public int getCode() {
        return code;
    }

    @Override
    public String getStringCode() {
        return stringCode;
    }
}
