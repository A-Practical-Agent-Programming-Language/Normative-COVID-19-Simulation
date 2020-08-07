package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

/**
 * THe relationship of the person to the reference person of the household
 */
public enum Relationship implements CodeTypeInterface {

    REFERENCE_PERSON(0),
    SPOUSE(1),
    BIOLOGICAL_SON_OR_DAUGHTER(2),
    ADOPTED_SON_OR_DAUGHTER(3),
    STEPSON_OR_STEPDAUGHTER(4),
    BROTHER_OR_SISTER(5),
    FATHER_OR_MOTHER(6),
    GRANDCHILD(7),
    PARENT_IN_LAW(8),
    SON_IN_LAW_OR_DAUGHTER_IN_LAW(9),
    OTHER_RELATIVE(10),
    ROOMER_OR_BOARDER(11),
    HOUSEMATE_OR_ROOMMATE(12),
    UNMARRIED_PARTNER(13),
    FOSTER_CHILD(14),
    OTHER_NONRELATIVE(15),
    INSTITUTIONALIZED_GROUP_QUARTERS_POPULATION(16),
    NONINSTITUTIONALIZED_GROUP_QUARTERS_POPULATION(17);

    private final int code;

    Relationship(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
