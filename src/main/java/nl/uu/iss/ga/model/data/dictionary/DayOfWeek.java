package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum DayOfWeek implements CodeTypeInterface {

    SUNDAY(1),
    MONDAY(2),
    TUESDAY(3),
    WEDNSDAY(4),
    THURSDAY(5),
    FRIDAY(6),
    SATURDAY(7);

    private int code;

    DayOfWeek(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static DayOfWeek fromSecondsSinceSundayMidnight(int secondsSinceSundayMidnight) {
        return CodeTypeInterface.parseAsEnum(DayOfWeek.class, secondsSinceSundayMidnight / ActivityTime.SECONDS_IN_DAY + 1);
    }


}
