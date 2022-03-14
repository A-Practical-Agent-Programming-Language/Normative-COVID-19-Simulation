package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

/**
 * FES
 * Character
 * 1
 * Family type and employment status
 * b .N/A (GQ/vacant/not a family/same-sex married-couple families)
 * 1 .Married-couple family: Husband and wife in LF
 * 2 .Married-couple family: Husband in labor force, wife not in LF
 * 3 .Married-couple family: Husband not in LF, wife in LF
 * 4 .Married-couple family: Neither husband nor wife in LF
 * 5 .Other family: Male householder, no wife present, in LF
 * 6 .Other family: Male householder, no wife present, not in LF
 * 7  .Other family: Female householder, no husband present, in LF
 * 8 .Other family: Female householder, no husband present, not in LF
 */
public enum FamilyEmployment implements StringCodeTypeInterface {
    NA("b", null, null, null, null, null),
    MARRIED_BOTH("1", true, true, true, true, true),
    MARRIED_HUSBAND("2", true, true, true, true, false),
    MARIED_WIFE("3", true, true, false, true, true),
    MARRIED_NONE("4", true, true, false, true, false),
    OTHER_MALE_WORKING("5", false, true, true, false, false),
    OTHER_MALE_NOT_WORKING("6", false, true, false, false, false),
    OTHER_FEMALE_WORKING("7", false, false, false, true, true),
    OTHER_FEMALE_NOT_WORKING("8", false, false, false, true, false);

    private final int code;
    private final String stringCode;
    private final Boolean married;
    private final Boolean husbandPresent;
    private final Boolean husbandWorking;
    private final Boolean wifePresent;
    private final Boolean wifeWorking;

    FamilyEmployment(String code, Boolean married, Boolean husbandPresent, Boolean husbandWorking, Boolean wifePresent, Boolean wifeWorking) {
        this.stringCode = code;
        this.code = StringCodeTypeInterface.parseStringcode(this.stringCode);
        this.married = married;
        this.husbandPresent = husbandPresent;
        this.husbandWorking = husbandWorking;
        this.wifePresent = wifePresent;
        this.wifeWorking = wifeWorking;
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
