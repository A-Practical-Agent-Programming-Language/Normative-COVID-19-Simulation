package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum UnitsInStructure implements StringCodeTypeInterface {
    NA("BB"),
    MOBILE_HOME_OR_TRAILER("01"),
    ONE_FAMILY_HOUSE_DETACHED("02"),
    ONE_FAMILY_HOUSE_ATTACHED("03"),
    FOUR_APARTMENTS("2"),
    THREE_TO_4_APARTMENTS("05"),
    FIVE_TO_9_APARTMENTS("06"),
    TEN_TO_19_APARTMENTS("07"),
    TWENTY_TO_49_APARTMENTS("08"),
    FIFTY_OR_MORE_APARTMENTS("09"),
    BOAT_OR_RV_OR_VAN_OR_ETC("10");

    private final int code;
    private final String stringCode;

    UnitsInStructure(String code) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(code);
    }

    @Override
    public String getStringCode() {
        return null;
    }

    @Override
    public int getCode() {
        return 0;
    }
}
