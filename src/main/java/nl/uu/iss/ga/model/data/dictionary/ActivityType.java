package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum ActivityType implements CodeTypeInterface {

    TRIP(0),
    HOME(1),
    WORK(2),
    SHOP(3),
    OTHER(4),
    SCHOOL(5),
    COLLEGE(6),
    RELIGIOUS(7); // TODO This occurs in the data?

    private final int code;

    ActivityType(int activityCode) {
        this.code = activityCode;
    }

    @Override
    public int getCode() {
        return code;
    }
}
