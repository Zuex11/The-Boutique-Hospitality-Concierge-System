package models;


public class Reservation {
    private int reservationId;
    private int guestId;
    private int suiteId;
    private LocalDate checkIn;
    private LocalDate checkOut;

    public Reservation(int guestId, int suiteId, LocalDate checkIn, LocalDate checkOut) {
        this.guestId = guestId;
        this.suiteId = suiteId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
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
}
