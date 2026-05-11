package models;


import java.time.LocalDate;

public class ReservationExperience {
    private int resExpId;
    private int experienceId;
    private int reservationId;
    private int coniergeId;
    private double actualCost;
    private LocalDate bookedDate;
    public ReservationExperience(int reservationId, int experienceId, int coniergeId, double actualCost, LocalDate bookedDate) {
        this.reservationId = reservationId;
        this.experienceId = experienceId;
        this.coniergeId = coniergeId;
        this.actualCost = actualCost;
        this.bookedDate = bookedDate;
    }
    public int getResExpId() {
        return resExpId;
    }

    public int getReservationId() {
        return reservationId;
    }

    public int getExperienceId() {
        return experienceId;
    }

    public int getConciergeId() {
        return coniergeId;
    }
    public double getActualCost() {
        return actualCost;
    }
    public void setActualCost(double actualCost) {
        this.actualCost = actualCost;
    }
    public LocalDate getBookedDate() {
        return bookedDate;
    }

    public void setResExpId(int resExpId) {
        this.resExpId = resExpId;
    }
}