package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.ActivityTime;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum DayOfWeek implements CodeTypeInterface {

    MONDAY(1),
    TUESDAY(2),
    WEDNSDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);


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
