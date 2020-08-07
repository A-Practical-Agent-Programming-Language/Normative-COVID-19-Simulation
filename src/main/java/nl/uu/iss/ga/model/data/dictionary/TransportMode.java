package main.java.nl.uu.iss.ga.model.data.dictionary;

import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;

public enum TransportMode implements CodeTypeInterface {
    NOT_ASCERTAINED(-9),
    DONT_KNOW(-8),
    NO_ANSWER(-7),
    WALK(1),
    BICYCLE(2),
    CAR(3),
    SUV(4),
    VAN(5),
    PICKUP_TRUCK(6),
    GOLF_CART_OR_SEGWAY(7),
    MOTORCYCLE_OR_MOPED(8),
    RV(9),
    SCHOOL_BUS(10),
    PUBLIC_OR_COMMUTER_BUS(11),
    PARATRANSIT_OR_DIAL_A_RIDE(12),
    PRIVATE_OR_CHARTER_OR_TOUR_OR_SHUTTLE_BUS(13),
    CITY_TO_CITY_BUS(14),
    AMTRAK_OR_COMMUTER_RAIL(15),
    SUBWAY_OR_ELEVATED_OR_LIGHT_RAIL_OR_STREET_CAR(16),
    TAXI_OR_LIMO(17),
    RENTAL_CAR(18),
    AIRPLANE(19),
    BOAT_OR_FERRY_OR_WATER_TAXI(20),
    SOMETHING_ELSE(97);

    private final int code;

    TransportMode(int transportModeCode) {
        this.code = transportModeCode;
    }

    public int getCode() {
        return code;
    }
}
