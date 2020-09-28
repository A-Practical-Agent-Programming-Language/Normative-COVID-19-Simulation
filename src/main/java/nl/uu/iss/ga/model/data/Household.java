package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.*;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;

import java.util.Map;

public class Household {

    private static final String VA_HOUSEHOLD_HEADERS =
            "admin1,admin2,admin3,admin4,hid,serialno,puma,record_type,hh_unit_wt,hh_size,vehicles,hh_income,units_in_structure,business,heating_fuel,household_language,family_type_and_employment_status,workers_in_family,rlid,residence_longitude,residence_latitude";
    private static final String[] VA_HOUSEHOLD_HEADER_INDICES = VA_HOUSEHOLD_HEADERS.split(ParserUtil.SPLIT_CHAR);

    //    private final int admin1; // TODO translate
//    private final int admin2; // TODO translate
//    private final int admin3; // TODO translate
//    private final int admin4; // TODO translate

    private final Long hid;

    /**
     * Housing unit/GQ person serial number
     * 2013000000001..2017999999999 .Unique identifier
     */
    private final int serialno;

    /**
     * Public use microdata area code (PUMA) based on 2010 Census definition
     * (areas with population of 100,000 or more, use with ST for unique code)
     * 00100..70301 .Public use microdata area codes
     */
    private final int puma;

    /**
     * Perhaps? Lot's of replicates
     *
     * WGTP1 Numeric 5
     * Housing Unit Weight replicate 1
     * -9999..09999 .Integer weight of housing unit
     */
//    private final int hh_unit_wt; //

    /**
     * Lot size
     * b .N/A (GQ/not a one-family house or mobile home)
     * 1 .House on less than one acre
     * 2 .House on one to less than ten acres
     * 3 .House on ten or more acres
     */
    private final UnitSize hh_size;

    /**
     * Vehicles (1 ton or less) available
     * b .N/A (GQ/vacant)
     * 0 .No vehicles
     * 1 .1 vehicle
     * 2 .2 vehicles
     * 3 .3 vehicles
     * 4 .4 vehicles
     * 5 .5 vehicles
     * 6 .6 or more vehicles
     *
     * Note, technically, more than 6 vehicles is a possibility, but let's ignore that to avoid
     * creating ENUMs for everything
     */
    private final int vehicles; // TODO translate

    /**
     * TODO some gross figure per year? Or net or per month? Lots of entries in PUMS dictionary
      */
    private final int hh_income;

    /**
     * BLD Character
     * 2
     * Units in structure
     * bb .N/A (GQ)
     * 01 .Mobile home or trailer
     * 02 .One-family house detached
     * 03 .One-family house attached
     * 04 .2 Apartments
     * 05 .3-4 Apartments
     * 06 .5-9 Apartments
     * 07 .10-19 Apartments
     * 08 .20-49 Apartments
     * 09 .50 or more apartments
     * 10 .Boat, RV, van, etc.
     */
    private final UnitsInStructure units_in_structure;

    /**
     * BUS Character
     * 1
     * Business or medical office on property
     * b .N/A (GQ/not a one-family house or mobile home)
     * 1 .Yes
     * 2 .No
     * 9 .Case is from 2016 or later
      */
    private final BusinessOnProperty business;

    /**
     * HFL Numeric
     * 1
     * House heating fuel
     * b .N/A (GQ/vacant)
     * 1 .Utility gas
     * 2 .Bottled, tank, or LP gas
     * 3 .Electricity
     * 4 .Fuel oil, kerosene, etc.
     * 5 .Coal or coke
     * 6 .Wood
     * 7 .Solar energy
     * 8 .Other fuel
     * 9 .No fuel used
     */
    private final Fuel heating_fuel;

    /**
     * HHL Character
     * 1
     * Household language
     * b .N/A (GQ/vacant)
     * 1 .English only
     * 2 .Spanish
     * 3 .Other Indo-European languages
     * 4 .Asian and Pacific Island languages
     * 5 .Other language
     */
    private final Language household_language;

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
    private final FamilyEmployment family_type_and_employment_status;

    /**
     * Treat as direct number. Just convert "b" to 0
     *
     * WIF
     * Character
     * 1
     * Workers in family during the past 12 months
     * b .N/A (GQ/vacant/non-family household)
     * 0 .No workers
     * 1 .1 worker
     * 2 .2 workers
     * 3 .3 or more workers in family
     */
    private final int workers_in_family;

    public Household(
            Long hid,
            int serialno,
            int puma,
            UnitSize hh_size,
            int vehicles,
            int hh_income,
            UnitsInStructure units_in_structure,
            BusinessOnProperty business,
            Fuel heating_fuel,
            Language household_language,
            FamilyEmployment family_type_and_employment_status,
            int workers_in_family) {
        this.hid = hid;
        this.serialno = serialno;
        this.puma = puma;
        this.hh_size = hh_size;
        this.vehicles = vehicles;
        this.hh_income = hh_income;
        this.units_in_structure = units_in_structure;
        this.business = business;
        this.heating_fuel = heating_fuel;
        this.household_language = household_language;
        this.family_type_and_employment_status = family_type_and_employment_status;
        this.workers_in_family = workers_in_family;
    }

    public Long getHid() {
        return hid;
    }

    public int getSerialno() {
        return serialno;
    }

    public int getPuma() {
        return puma;
    }

    public UnitSize getHh_size() {
        return hh_size;
    }

    public int getVehicles() {
        return vehicles;
    }

    public int getHh_income() {
        return hh_income;
    }

    public UnitsInStructure getUnits_in_structure() {
        return units_in_structure;
    }

    public BusinessOnProperty getBusiness() {
        return business;
    }

    public Fuel getHeating_fuel() {
        return heating_fuel;
    }

    public Language getHousehold_language() {
        return household_language;
    }

    public FamilyEmployment getFamily_type_and_employment_status() {
        return family_type_and_employment_status;
    }

    public int getWorkers_in_family() {
        return workers_in_family;
    }

    public static Household fromCSVLine(String line) {
        Map<String ,String> keyValue = ParserUtil.zipLine(VA_HOUSEHOLD_HEADER_INDICES, line);
        return new Household(
                ParserUtil.parseAsLong(keyValue.get("hid")),
                ParserUtil.parseAsInt(keyValue.get("serialno")),
                ParserUtil.parseAsInt(keyValue.get("puma")),
                StringCodeTypeInterface.parseAsEnum(UnitSize.class, keyValue.get("hh_size")),
                ParserUtil.parseAsInt(keyValue.get("vehicles")),
                ParserUtil.parseAsInt(keyValue.get("hh_income")),
                StringCodeTypeInterface.parseAsEnum(UnitsInStructure.class, keyValue.get("units_in_structure")),
                StringCodeTypeInterface.parseAsEnum(BusinessOnProperty.class, keyValue.get("business")),
                StringCodeTypeInterface.parseAsEnum(Fuel.class, keyValue.get("heating_fuel")),
                StringCodeTypeInterface.parseAsEnum(Language.class, keyValue.get("household_language")),
                StringCodeTypeInterface.parseAsEnum(FamilyEmployment.class, keyValue.get("family_type_and_employment_status")),
                ParserUtil.parseAsInt(keyValue.get("workers_in_famuly"))
        );
    }

    /**
     * NON-data driven parameters
     */
    private Boolean liberal = null; // false if conservative

    public boolean isLiberal() {
        return liberal;
    }

    public void setLiberal(boolean liberal) {
        this.liberal = liberal;
    }
}
