package main.java.nl.uu.iss.ga.model.data.dictionary;


import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum DetailedActivity implements CodeTypeInterface {
    /** TRIP **/
    TRIP(0),

    /** HOME **/
    REGULAR_HOME_ACTIVITIES(1),
    WORK_FROM_HOME(2),

    /** WORK **/
    WORK(3),
    WORK_RELATED_MEETING_TRIP(4),

    /** SHOP **/
    BUY_GOODS(11),

    /** OTHER **/
    VOLUNTEER_ACTIVITIES(5),
    DROP_OFF_PICK_UP_SOMEONE(6),
    CHANGE_TYPE_OF_TRANSPORTATION(7),
    BUY_SERVICES(12),
    BUY_MEALS(13),
    OTHER_GENERAL_ERRANDS(14),
    RECREATIONAL_ACTIVITIES(15),
    EXERCISE(16),
    VISIT_FRIENDS_OR_RELATIVES(17),
    HEALTH_CARE_VISIT(18),
    SOMETHING_ELSE(97),

    /** NOT IN SAMPLE **/
    ATTEND_SCHOOL_AS_A_STUDENT(8),
    ATTEND_CHILD_CARE(9),
    ATTEND_ADULT_CARE(10),

    /** with non-existent activity_type 7 **/
    RELIGIOUS_OR_OTHER_COMMUNITY_ACTIVITIES(19);

    private final int code;

    DetailedActivity(int code) {
        this.code = code;
    }

    @Override
    public int getCode() {
        return this.code;
    }
}
