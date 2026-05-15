package models;

/**
 * Represents a concierge staff member assigned to a hotel in the MIRAGE system.
 *
 * <p>A {@code Concierge} is linked to exactly one {@link Hotel} and is
 * responsible for managing guest experiences at that property. When a
 * {@code ReservationExperience} is created, a concierge from the same hotel
 * must be assigned — this hotel-matching constraint is enforced by
 * {@code ExperienceDAO.insertExperience()}.</p>
 *
 * <p><b>Database mapping:</b> {@code concierge} table.</p>
 */
public class Concierge {

    /** Auto-generated primary key assigned by the database. */
    private int conciergeId;

    /** Full name of the concierge staff member. */
    private String fullName;

    /** Foreign key referencing the hotel this concierge is assigned to. */
    private int hotelId;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code Concierge} assigned to a specific hotel.
     * {@code conciergeId} is left at {@code 0} until the record is persisted
     * and the key is set via {@link #setConciergeId(int)}.
     *
     * @param fullName the concierge's full name; must not be {@code null} or blank
     * @param hotelId  the ID of the hotel this concierge is assigned to; must be positive
     */
    public Concierge(String fullName, int hotelId) {
        this.fullName = fullName;
        this.hotelId = hotelId;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the ID of the hotel this concierge is assigned to.
     *
     * @return hotel ID; always positive for a valid concierge
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * Returns the database primary key for this concierge.
     *
     * @return the concierge ID, or {@code 0} if not yet persisted
     */
    public int getConciergeId() {
        return conciergeId;
    }

    /**
     * Returns the full name of this concierge.
     *
     * @return full name string; never {@code null} for a properly constructed instance
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Updates the full name of this concierge.
     *
     * @param fullName the new full name; must not be {@code null} or blank
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Sets the database-generated primary key for this concierge.
     * Called by {@code ConciergeDAO} after a successful {@code INSERT}.
     *
     * @param id the auto-generated concierge ID; must be a positive integer
     */
    public void setConciergeId(int id) {
        this.conciergeId = id;
    }
}
