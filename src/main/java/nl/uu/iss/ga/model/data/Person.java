package main.java.nl.uu.iss.ga.model.data;

import main.java.nl.uu.iss.ga.model.data.dictionary.*;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.CodeTypeInterface;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.ParserUtil;
import main.java.nl.uu.iss.ga.model.data.dictionary.util.StringCodeTypeInterface;
import nl.uu.cs.iss.ga.sim2apl.core.agent.Context;

import java.util.Map;

public class Person implements Context {
    private static final String VA_PERSON_HEADERS =
            "hid,pid,serialno,person_number,record_type,age,relationship,sex,school_enrollment,grade_level_attending,employment_status,employment_socp,cell_id,designation";
    private static final String[] VA_PERSON_HEADER_INDICES = VA_PERSON_HEADERS.split(ParserUtil.SPLIT_CHAR);

    private Household household;
    private Long pid;
    private int serialno;
    private int person_number;
    private int age;
    private Relationship relationship;
    private Gender sex;
    private SchoolEnrollment school_enrollment;
    private GradeLevel grade_level;
    private EmploymentStatus employment_status;
    private String occupation_socp; // TODO translate and get class instead of occupation itself

    public Person(
            Household household,
            Long pid,
            int serialno,
            int age,
            Relationship relationship,
            Gender sex,
            SchoolEnrollment school_enrollment,
            GradeLevel grade_level,
            EmploymentStatus employment_status,
            String occupation_socp
    ) {
        this.household = household;
        this.pid = pid;
        this.serialno = serialno;
        this.age = age;
        this.relationship = relationship;
        this.sex = sex;
        this.school_enrollment = school_enrollment;
        this.grade_level = grade_level;
        this.employment_status = employment_status;
        this.occupation_socp = occupation_socp;
    }

    public Household getHousehold() {
        return household;
    }

    public Long getPid() {
        return pid;
    }

    public int getSerialno() {
        return serialno;
    }

    public int getPerson_number() {
        return person_number;
    }

    public int getAge() {
        return age;
    }

    public Relationship getRelationship() {
        return relationship;
    }

    public Gender getSex() {
        return sex;
    }

    public SchoolEnrollment getSchool_enrollment() {
        return school_enrollment;
    }

    public GradeLevel getGrade_level() {
        return grade_level;
    }

    public EmploymentStatus getEmployment_status() {
        return employment_status;
    }

    public String getOccupation_socp() {
        return occupation_socp;
    }

    public static Person fromLine(Map<Long, Household> households, String line) {
        Map<String, String> keyValue = ParserUtil.zipLine(VA_PERSON_HEADER_INDICES, line);
        return new Person(
                households.get(ParserUtil.parseAsLong(keyValue.get("hid"))),
                ParserUtil.parseAsLong(keyValue.get("pid")),
                ParserUtil.parseAsInt(keyValue.get("serialno")),
                ParserUtil.parseAsInt(keyValue.get("age")),
                CodeTypeInterface.parseAsEnum(Relationship.class, keyValue.get("relationship")),
                CodeTypeInterface.parseAsEnum(Gender.class, keyValue.get("sex")),
                StringCodeTypeInterface.parseAsEnum(SchoolEnrollment.class, keyValue.get("school_enrollment")),
                StringCodeTypeInterface.parseAsEnum(GradeLevel.class, keyValue.get("grade_level_attending")),
                StringCodeTypeInterface.parseAsEnum(EmploymentStatus.class, keyValue.get("employment_status")),
                keyValue.get("occupation_socp")
        );
    }

    public double fixedAgeRisk() {
        // https://www.desmos.com/calculator/fry7sc859n
        double x = Math.pow(Math.E, (.1 * this.age - 5));
        return x / (x + 1);
    }
}
