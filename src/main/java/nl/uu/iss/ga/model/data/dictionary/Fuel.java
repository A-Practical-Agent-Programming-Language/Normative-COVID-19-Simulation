package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

public enum Fuel implements StringCodeTypeInterface {
    NA("b"),  //(GQ/vacant)
    UTILITY_GAS("1"),
    BOTTLED_OR_TANK_OR_OR_LP_GAS("2"),
    ELECTRICITY("3"),
    FUEL_OIL_OR_KEROSENE_OR_ETC("4"),
    COAL_OR_COKE("5"),
    WOOD("6"),
    SOLAR_ENERGY("7"),
    OTHER_FUEL("8"),
    NO_FUEL_USED("9");

    private final int code;
    private final String stringCode;

    Fuel(String code) {
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
