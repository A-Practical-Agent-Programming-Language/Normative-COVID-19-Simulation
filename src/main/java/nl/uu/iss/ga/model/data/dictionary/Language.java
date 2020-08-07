package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

/**
 * HHL Character
 * 1
 * Household language
 * b .N/A (GQ/vacant)
 * 1 .English only
 * 2 .Spanish
 * 3 .Other Indo-European languages
 * 4 .Asian and Pacific Island languages
 * 5 .Other language
 *
 * TODO does this mean languages besides English? Or just English?
 */
public enum Language implements StringCodeTypeInterface {
    NA("b"),  //(GQ/vacant)
    ENGLISH_ONLY("1"),
    SPANISH("2"),
    OTHER_INDOEUROPEAN_LANGUAGES("3"),
    ASIAN_AND_PACIFIC_ISLAND_LANGUAGES("4"),
    OTHER_LANGUAGE("5");

    private String stringCode;
    private int code;

    Language(String code) {
        this.code = StringCodeTypeInterface.parseStringcode(code);
        this.stringCode = code;
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
