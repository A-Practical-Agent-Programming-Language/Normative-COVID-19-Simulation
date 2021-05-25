package main.java.nl.uu.iss.ga.model.data.dictionary;

public enum  Designation {
    military,
    government,
    retail,
    none,
    education,
    medical,
    care_facilitation,
    // DMV is not in the dictionary. We mark the government location with the highest visit count as the DMV location in each county
    // DMV is not a designation of workers
    dmv
}
