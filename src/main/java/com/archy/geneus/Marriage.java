package com.archy.geneus;

import java.time.LocalDate;

public class Marriage {

    protected final Person spouse;

    protected LocalDate startDate;
    protected String startArea;
    protected String startCountry;

    private LocalDate endDate;
    private String endArea;
    private String endCountry;


    public boolean isActive() {
        return endDate == null;
    }

    public Marriage(Person spouse) {
        this.spouse = spouse;
    }

    public Person getSpouse() {
        return spouse;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public String getStartArea() {
        return startArea;
    }

    public void setStartArea(String startArea) {
        this.startArea = startArea;
    }

    public String getStartCountry() {
        return startCountry;
    }

    public void setStartCountry(String startCountry) {
        this.startCountry = startCountry;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getEndArea() {
        return endArea;
    }

    public String getEndCountry() {
        return endCountry;
    }

    public void setEndArea(String endArea) {
        this.endArea = endArea;
    }

    public void setEndCountry(String endCountry) {
        this.endCountry = endCountry;
    }

    @Override
    public String toString() {
        return "Marriage{" +
               "spouse=(" + spouse.getDisplayName() + ", " + spouse.getId() + ")" +
               ", startDate=" + startDate +
               ", startArea='" + startArea + '\'' +
               ", startCountry='" + startCountry + '\'' +
               ", endDate=" + endDate +
               ", endArea='" + endArea + '\'' +
               ", endCountry='" + endCountry + '\'' +
               '}';
    }
}