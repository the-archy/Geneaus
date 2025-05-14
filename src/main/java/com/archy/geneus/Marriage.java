package com.archy.geneus;

import java.time.LocalDate;

public class Marriage {

    protected final Person spouse;

    private boolean active;

    protected LocalDate startDate;
    protected String startArea;
    protected String startCountry;

    private LocalDate endDate;
    private String endArea;
    private String endCountry;

    public Marriage(Person spouse, boolean active) {
        this.spouse = spouse;
        this.active = active;
    }

    public Person getSpouse() {
        return spouse;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
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

    public void setEndArea(String endArea) {
        this.endArea = endArea;
    }
}