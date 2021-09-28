package main.java.nl.uu.iss.ga.model.norm;

import main.java.nl.uu.iss.ga.model.norm.modal.AllowWearMaskNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.EncourageDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.MaintainDistanceNorm;
import main.java.nl.uu.iss.ga.model.norm.modal.WearMaskPublicIndoorsNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.EncourageTeleworkNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.IfSickStayHomeNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.KeepGroupsSmallNorm;
import main.java.nl.uu.iss.ga.model.norm.nonregimented.StayHomeNorm;
import main.java.nl.uu.iss.ga.model.norm.regimented.*;

import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class NormFactory {

    private static final Logger LOGGER = Logger.getLogger(NormFactory.class.getName());

    private static final List<String> ALL_NORM_STRINGS = Arrays.asList(
            "EncourageTelework", "AllowWearMask", "SchoolsClosed[K12]", "SchoolsClosed[K12;HIGHER_EDUCATION]",
            "SmallGroups[100,public]", "SmallGroups[10,public]", "SmallGroups[10,PP]", "SmallGroups[50,PP]",
            "SmallGroups[250,PP]", "EncourageSocialDistance", "StayHomeSick", "StayHome[age>65]", "StayHome[all]",
            "ReduceBusinessCapacity[10]", "ReduceBusinessCapacity[50%]", "ReduceBusinessCapacity[75%]",
            "BusinessClosed[7 DMV offices]", "BusinessClosed[7 DMV offices;NEB]", "TakeawayOnly", "MaintainDistance", "BeachesClosed[all]",
            "BeachesClosed[VirginiaBeach]", "EmployeesWearMask", "TakeawayAndOutdoorOnly", "WearMasInPublicIndoor",
            "ReduceBusinessHours", "ReduceHigherEducCapacity"
    );

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
//                return EncourageSocialDistanceFromCSVLine(keyValue);
                return MaintainDistanceFromCSVLine(keyValue);
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
            case "WearMasInPublicIndoor":
                return WearMasInPublicIndoor(keyValue);
            case "ReduceBusinessHours":
                return ReduceBusinessHours(keyValue);
            case "ReduceHigherEducCapacity":
                return ReduceHigherEducationCapacityNorm(keyValue);
            case "":
            default:
                LOGGER.log(Level.WARNING, String.format("Could not map norm identifier to norm class: \"%s\"%n", keyValue.get("norm")));
                return null;
        }
    }

    public static Map<String, List<Norm>> instantiateAllNorms() {
        Map<String, List<Norm>> allNormsCodes = new HashMap<>();
        for(String normString : ALL_NORM_STRINGS) {
            List<Map<String, String>> keyValues = new ArrayList<>();
            if (normString.contains("[")) {
                String normName = normString.substring(0, normString.indexOf('['));
                String[] normParams = normString.substring(normString.indexOf('[') + 1, normString.indexOf(']')).split(";");
                for(String normParam : normParams) {
                    keyValues.add(Map.of("norm", normName, "param", normParam));
                }
            } else {
                keyValues.add(Map.of("norm", normString));
            }
            allNormsCodes.put(normString, keyValues.stream().map(NormFactory::fromCSVLine).collect(Collectors.toList()));
        }
        return allNormsCodes;
    }

    public static ReduceHigherEducationCapacityNorm ReduceHigherEducationCapacityNorm(Map<String, String> keyValue) {
        return new ReduceHigherEducationCapacityNorm();
    }

    public static ReduceBusinessHoursNorm ReduceBusinessHours(Map<String, String> keyValue) {
        return new ReduceBusinessHoursNorm(keyValue.get("param"));
    }

    public static WearMaskPublicIndoorsNorm WearMasInPublicIndoor(Map<String, String> keyValue) {
        return new WearMaskPublicIndoorsNorm();
    }

    public static EncourageTeleworkNorm EncourageTeleworkFromCSVLine(Map<String, String> keyValue) {
        return new EncourageTeleworkNorm();
    }

    public static AllowWearMaskNorm AllowWearMaskFromCSVLine(Map<String, String> keyValue) {
        return new AllowWearMaskNorm();
    }

    public static SchoolsClosed SchoolsClosedFromCSVLine(Map<String, String> keyValue) {
        return new SchoolsClosed(keyValue.get("param"));
    }

    public static KeepGroupsSmallNorm SmallGroupsFromCSVLine(Map<String, String> keyValue) {
        return new KeepGroupsSmallNorm(keyValue.get("param"));
    }

    @Deprecated
    public static EncourageDistanceNorm EncourageSocialDistanceFromCSVLine(Map<String, String> keyValue) {
        return new EncourageDistanceNorm();
    }

    public static IfSickStayHomeNorm StayHomeSickFromCSVLine(Map<String, String> keyValue) {
        return new IfSickStayHomeNorm();
    }

    public static StayHomeNorm StayHomeFromCSVLine(Map<String, String> keyValue) {
        return new StayHomeNorm(keyValue.get("param"));
    }

    public static ReduceBusinessCapacityNorm ReduceBusinessCapacityFromCSVLine(Map<String, String> keyValue) {
        return new ReduceBusinessCapacityNorm(keyValue.get("param"));
    }

    public static BusinessClosedNorm BusinessClosedFromCSVLine(Map<String, String> keyValue) {
        return new BusinessClosedNorm(keyValue.get("param"));
    }

    public static TakeawayOnly TakeawayOnlyFromCSVLine(Map<String, String> keyValue) {
        return new TakeawayOnly();
    }

    public static MaintainDistanceNorm MaintainDistanceFromCSVLine(Map<String, String> keyValue) {
        return new MaintainDistanceNorm();
    }

    public static BeachesClosedNorm BeachesClosedFromCSVLine(Map<String, String> keyValue) {
        return new BeachesClosedNorm();
    }

    public static EmployeesWearMaskNorm EmployeesWearMaskFromCSVLine(Map<String, String> keyValue) {
        return new EmployeesWearMaskNorm();
    }

    public static OutDoorActivitiesOnly TakeawayAndOutdoorOnlyFromCSVLine(Map<String, String> keyValue) {
        return new OutDoorActivitiesOnly();
    }
}
