package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

/**
 * Lot size
 * b .N/A (GQ/not a one-family house or mobile home)
 * 1 .House on less than one acre
 * 2 .House on one to less than ten acres
 * 3 .House on ten or more acres
 */
public enum UnitSize implements StringCodeTypeInterface {

    NA("b"), // (GQ/not a one-family house or mobile home)
    HOUSE_ON_LESS_THAN_ONE_ACRE("1"),
    HOUSE_ON_ONE_TO_LESS_THAN_TEN_ACRES("2"),
    HOUSE_ON_TEN_OR_MORE_ACRES("3");

    private final int code;
    private final String stringCode;

    UnitSize(String code) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(code);
    }

    @Override
    public String getStringCode() {
        return this.stringCode;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
