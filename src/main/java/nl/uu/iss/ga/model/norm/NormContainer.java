package main.java.nl.uu.iss.ga.model.norm;

import java.time.LocalDate;

public class NormContainer {
    private Norm norm;
    private LocalDate startDate;
    private LocalDate endDate;
    private String comment;

    public NormContainer(Norm norm, LocalDate startDate, LocalDate endDate, String comment) {
        this.norm = norm;
        this.startDate = startDate;
        this.endDate = endDate;
        this.comment = comment;
    }
}
