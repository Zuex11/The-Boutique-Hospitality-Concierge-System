package models;

import java.time.LocalDate;

/**
 * Represents a suite reservation made by a guest in the MIRAGE system.
 *
 * <p>A {@code Reservation} captures the guest, the suite booked, the stay
 * window (check-in / check-out dates), the computed total room cost, and
 * the current lifecycle status. Instances are created by the application
 * when booking a suite and are hydrated from the database by
 * {@code ReservationDAO}.</p>
 *
 * <p><b>Database mapping:</b> {@code reservation} table.</p>
 */
public class Reservation {

    /** Auto-generated primary key assigned by the database. */
    private int reservationId;

    /** Foreign key referencing the guest who made this reservation. */
    private int guestId;

    /** Foreign key referencing the suite that was booked. */
    private int suiteId;

    /**
     * Total room charge for the stay in USD, calculated as:
     * {@code nights × suite_class.nightly_rate}.
     * Computed and persisted by {@code ReservationDAO.insertReservation()}.
     */
    private int total_cost;

    /** The date the guest is scheduled to check in. */
    private LocalDate checkIn;

    /** The date the guest is scheduled to check out. */
    private LocalDate checkOut;

    /**
     * Lifecycle status of the reservation.
     * Typical values: {@code "confirmed"}, {@code "checked-out"}, {@code "cancelled"}.
     * Defaults to {@code "confirmed"} on insert (enforced by the database).
     */
    private String status;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code Reservation} for the given guest and suite.
     * {@code reservationId} is left at its default ({@code 0}) until the
     * record is persisted and the key is set via {@link #setReservationId(int)}.
     * {@code total_cost} and {@code status} are populated after persistence.
     *
     * @param guestId  the ID of the guest making the reservation; must be positive
     * @param suiteId  the ID of the suite being booked; must be positive
     * @param checkIn  the check-in date; must not be {@code null} or in the past
     * @param checkOut the check-out date; must be strictly after {@code checkIn}
     */
    public Reservation(int guestId, int suiteId, LocalDate checkIn, LocalDate checkOut) {
        this.guestId = guestId;
        this.suiteId = suiteId;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Sets the total room cost for this reservation.
     * Called by {@code ReservationDAO} after reading the computed cost
     * from the database result set.
     *
     * @param cost the total room charge in USD
     */
    public void totalCostSetter(int cost) {
        this.total_cost = cost;
    }

    /**
     * Returns the total room cost for this reservation.
     *
     * @return total cost in USD, or {@code 0} if not yet set
     */
    public int totalCostGetter() {
        return this.total_cost;
    }

    /**
     * Sets the database-generated primary key for this reservation.
     * Called by {@code ReservationDAO} after a successful {@code INSERT}.
     *
     * @param id the auto-generated reservation ID; must be a positive integer
     */
    public void setReservationId(int id) {
        this.reservationId = id;
    }

    /**
     * Returns the database primary key for this reservation.
     *
     * @return the reservation ID, or {@code 0} if not yet persisted
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Returns the ID of the guest who made this reservation.
     *
     * @return guest ID; always positive for a valid reservation
     */
    public int getGuestId() {
        return guestId;
    }

    /**
     * Returns the ID of the suite booked by this reservation.
     *
     * @return suite ID; always positive for a valid reservation
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * Returns the check-in date for this reservation.
     *
     * @return check-in {@link LocalDate}; never {@code null} for a valid reservation
     */
    public LocalDate getCheckIn() {
        return checkIn;
    }

    /**
     * Returns the check-out date for this reservation.
     *
     * @return check-out {@link LocalDate}; always strictly after {@code checkIn}
     */
    public LocalDate getCheckOut() {
        return checkOut;
    }

    /**
     * Updates the check-in date.
     *
     * @param checkIn the new check-in date; must not be {@code null}
     */
    public void setCheckIn(LocalDate checkIn) {
        this.checkIn = checkIn;
    }

    /**
     * Updates the check-out date.
     *
     * @param checkOut the new check-out date; must be strictly after check-in
     */
    public void setCheckOut(LocalDate checkOut) {
        this.checkOut = checkOut;
    }

    /**
     * Sets the lifecycle status of this reservation.
     * Called by {@code ReservationDAO} when hydrating from the database.
     *
     * @param status one of {@code "confirmed"}, {@code "checked-out"},
     *               or {@code "cancelled"}
     */
    public void statusSetter(String status) {
        this.status = status;
    }

    /**
     * Returns the lifecycle status of this reservation.
     *
     * @return the status string; may be {@code null} if not yet set
     */
    public String statusGetter() {
        return this.status;
    }
}
