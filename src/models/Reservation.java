package models;


import java.time.LocalDate;

public class Reservation {
    private int reservationId;
    private int guestId;
    private int suiteId;
    private int total_cost;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private String status;

    public Reservation(int guestId, int suiteId, LocalDate checkIn, LocalDate checkOut) {
        this.guestId = guestId;
        this.suiteId = suiteId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    public void totalCostSetter(int cost)
    {
        this.total_cost = cost;
    }
    
    public int totalCostGetter()
    {
        return this.total_cost;
    }

    setReservationId(int id)
    {
        this.reservationId = id;
    }

    public int getReservationId() {
        return reservationId;
    }

    public int getGuestId() {
        return guestId;
    }

    public int getSuiteId() {
        return suiteId;
    }

    public LocalDate getCheckIn() {
        return checkIn;
    }

    public LocalDate getCheckOut() {
        return checkOut;
    }

    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    public void statusSetter(String status)
    {
        this.status = status;
    }

    public String statusGetter()
    {
        return this.status;
    }
}
