package nl.uu.iss.ga.model.data.dictionary;

import nl.uu.iss.ga.model.data.ActivityTime;
import nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

import java.time.LocalDate;

public enum DayOfWeek implements CodeTypeInterface {

    MONDAY(1),
    TUESDAY(2),
    WEDNESDAY(3),
    THURSDAY(4),
    FRIDAY(5),
    SATURDAY(6),
    SUNDAY(7);


    private int code;
    private int dayStartsSecondsSinceSundayMidnight;

    DayOfWeek(int code) {
        this.code = code;
        this.dayStartsSecondsSinceSundayMidnight = (this.code - 1) * 24 * 60 * 60;
    }

    @Override
    public int getCode() {
        return code;
    }

    public static DayOfWeek fromSecondsSinceSundayMidnight(int secondsSinceSundayMidnight) {
        return CodeTypeInterface.parseAsEnum(DayOfWeek.class, secondsSinceSundayMidnight / ActivityTime.SECONDS_IN_DAY + 1);
    }

    public int getSecondsSinceMidnightForDayStart() {
        return this.dayStartsSecondsSinceSundayMidnight;
    }

    public static DayOfWeek fromDate(LocalDate date) {
        return DayOfWeek.valueOf(date.getDayOfWeek().toString());
    }
}
