package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.norm.modal.AllowWearMaskNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.EncourageDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.StayHomeNorm;
import main.java.nl.uu.iss.ga.model.norm.regimented.*;

import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public class NormFactory {

    private static final Logger LOGGER = Logger.getLogger(NormFactory.class.getName());

    public static Norm fromCSVLine(Map<String, String> keyValue) {
        switch (keyValue.get("norm")) {
            case "EncourageTelework":
                return EncourageTeleworkFromCSVLine(keyValue);
            case "AllowWearMask":
                return AllowWearMaskFromCSVLine(keyValue);
            case "SchoolsClosed":
                return SchoolsClosedFromCSVLine(keyValue);
            case "SmallGroups":
                return SmallGroupsFromCSVLine(keyValue);
            case "EncourageSocialDistance":
                return EncourageSocialDistanceFromCSVLine(keyValue);
            case "StayHomeSick":
                return StayHomeSickFromCSVLine(keyValue);
            case "StayHome":
                return StayHomeFromCSVLine(keyValue);
            case "ReduceBusinessCapacity":
                return ReduceBusinessCapacityFromCSVLine(keyValue);
            case "BusinessClosed":
                return BusinessClosedFromCSVLine(keyValue);
            case "TakeawayOnly":
                return TakeawayOnlyFromCSVLine(keyValue);
            case "MaintainDistance":
                return MaintainDistanceFromCSVLine(keyValue);
            case "BeachesClosed":
                return BeachesClosedFromCSVLine(keyValue);
            case "EmployeesWearMask":
                return EmployeesWearMaskFromCSVLine(keyValue);
            case "TakeawayAndOutdoorOnly":
                return TakeawayAndOutdoorOnlyFromCSVLine(keyValue);
            default:
                LOGGER.log(Level.WARNING, "Could not map norm identifier to norm class: " + keyValue.get("norm"));
                return null;
        }
    }

    public static EncourageTeleworkNorm EncourageTeleworkFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static AllowWearMaskNorm AllowWearMaskFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static SchoolsClosed SchoolsClosedFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static KeepGroupsSmallNorm SmallGroupsFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static EncourageDistanceNorm EncourageSocialDistanceFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static IfSickStayHomeNorm StayHomeSickFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static StayHomeNorm StayHomeFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static ReduceBusinessCapacityNorm ReduceBusinessCapacityFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static BusinessClosedNorm BusinessClosedFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static TakeawayOnly TakeawayOnlyFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static MaintainDistanceNorm MaintainDistanceFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static BeachesClosedNorm BeachesClosedFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static EmployeesWearMaskNorm EmployeesWearMaskFromCSVLine(Map<String, String> keyValue) {
        return null;
    }

    public static TakeawayOnly TakeawayAndOutdoorOnlyFromCSVLine(Map<String, String> keyValue) {
        return null;
    }
}
