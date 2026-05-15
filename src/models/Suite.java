package models;

import java.time.LocalDate;

/**
 * Represents an individual bookable suite within a hotel in the MIRAGE system.
 *
 * <p>Each {@code Suite} belongs to exactly one {@link Hotel} and is assigned
 * exactly one {@link SuiteClass} that determines its nightly rate and amenities.
 * The suite number is a human-readable identifier unique within its hotel
 * (e.g. {@code "401-A"}).</p>
 *
 * <p>When loaded via {@code ReservationDAO.getAvailableSuites()}, additional
 * display fields ({@code hotelName}, {@code nextAvailable}) are populated
 * for use in the UI suite grid.</p>
 *
 * <p><b>Database mapping:</b> {@code suite} table.</p>
 */
public class Suite {

    /** Auto-generated primary key assigned by the database. */
    private int suiteId;

    /** Foreign key referencing the hotel this suite belongs to. */
    private int hotelId;

    /** Foreign key referencing the suite class (tier / nightly rate). */
    private int classId;

    /** Human-readable suite identifier within the hotel (e.g. {@code "302"}). */
    private String suiteNumber;

    /**
     * Display name of the owning hotel.
     * Populated by join queries in {@code ReservationDAO}; not stored in
     * the {@code suite} table itself.
     */
    private String hotelName;

    /**
     * The earliest date from which this suite is free of active reservations.
     * {@code null} means the suite is available immediately.
     * Populated by {@code ReservationDAO.getAvailableSuites()}.
     */
    private LocalDate nextAvailable;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code Suite} with its core identity fields.
     * {@code suiteId} is left at {@code 0} until the record is persisted
     * and the key is set via {@link #setSuiteId(int)}.
     *
     * @param hotelId     the ID of the hotel this suite belongs to; must be positive
     * @param classId     the ID of the suite class that defines its rate; must be positive
     * @param suiteNumber the human-readable suite number; must not be {@code null} or blank
     */
    public Suite(int hotelId, int classId, String suiteNumber) {
        this.hotelId = hotelId;
        this.classId = classId;
        this.suiteNumber = suiteNumber;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the ID of the hotel this suite belongs to.
     *
     * @return hotel ID; always positive for a valid suite
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * Updates the hotel association for this suite.
     *
     * @param hotelId the new hotel ID; must be positive
     */
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * Returns the ID of the suite class (tier) assigned to this suite.
     *
     * @return class ID; always positive for a valid suite
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Returns the database primary key for this suite.
     *
     * @return the suite ID, or {@code 0} if not yet persisted
     */
    public int getSuiteId() {
        return suiteId;
    }

    /**
     * Sets the database-generated primary key for this suite.
     * Called by {@code HotelDAO} after a successful {@code INSERT}.
     *
     * @param suiteId the auto-generated suite ID; must be a positive integer
     */
    public void setSuiteId(int suiteId) {
        this.suiteId = suiteId;
    }

    /**
     * Returns the human-readable suite number.
     *
     * @return suite number string; never {@code null} for a properly constructed instance
     */
    public String getSuiteNumber() {
        return suiteNumber;
    }

    /**
     * Returns the display name of the hotel that owns this suite.
     * This field is only populated when the suite is loaded via a JOIN query.
     *
     * @return hotel name, or {@code null} if not populated
     */
    public String getHotelName() {
        return hotelName;
    }

    /**
     * Sets the display name of the owning hotel.
     * Called by {@code ReservationDAO} when hydrating suite records via JOIN.
     *
     * @param hotelName the hotel's display name; may be {@code null}
     */
    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }

    /**
     * Returns the earliest date this suite becomes available for booking.
     *
     * @return the next available date, or {@code null} if the suite is
     *         currently free (no future or active reservations)
     */
    public LocalDate getNextAvailable() {
        return nextAvailable;
    }

    /**
     * Sets the next available date for this suite.
     * Called by {@code ReservationDAO.getAvailableSuites()} after computing
     * availability from active reservation records.
     *
     * @param nextAvailable the next free date, or {@code null} if available now
     */
    public void setNextAvailable(LocalDate nextAvailable) {
        this.nextAvailable = nextAvailable;
    }
}
