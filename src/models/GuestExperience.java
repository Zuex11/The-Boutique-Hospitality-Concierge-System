package models;

/**
 * Represents an experience offering available at a specific hotel in the MIRAGE system.
 *
 * <p>A {@code GuestExperience} is a bookable activity or service tied to a
 * particular hotel (e.g. a spa session, private dining, or a city tour).
 * Each experience has a base cost that serves as a reference price; the
 * actual cost charged to a reservation may differ and is stored in
 * {@link ReservationExperience#getActualCost()}.</p>
 *
 * <p>Experiences are scoped to a hotel — only experiences belonging to the
 * same hotel as the reservation can be booked for that reservation.
 * This constraint is enforced by {@code ExperienceDAO.insertExperience()}.</p>
 *
 * <p><b>Database mapping:</b> {@code guest_experience} table.</p>
 */
public class GuestExperience {

    /** Auto-generated primary key assigned by the database. */
    private int experienceId;

    /** Foreign key referencing the hotel that offers this experience. */
    private int hotelId;

    /** Display name of the experience (e.g. "Rooftop Yoga", "Private Dining"). */
    private String experienceName;

    /** Detailed description of the experience; may be empty for UI-only usage. */
    private String description;

    /**
     * Reference / base price of the experience in USD.
     * The actual amount billed to a reservation is stored separately in
     * {@code reservation_experience.actual_cost}.
     */
    private double baseCost;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code GuestExperience} for a specific hotel.
     * {@code experienceId} is left at {@code 0} until the record is persisted
     * and the key is set via {@link #setExperienceId(int)}.
     *
     * @param hotelId        the ID of the hotel offering this experience; must be positive
     * @param experienceName the display name of the experience; must not be {@code null} or blank
     * @param description    a detailed description; may be empty or {@code null}
     * @param baseCost       the reference price in USD; must be non-negative
     */
    public GuestExperience(int hotelId, String experienceName, String description, double baseCost) {
        this.hotelId = hotelId;
        this.experienceName = experienceName;
        this.description = description;
        this.baseCost = baseCost;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Sets the database-generated primary key for this experience.
     * Called by {@code ExperienceDAO} / {@code HotelDAO} after a successful {@code INSERT}.
     *
     * @param experienceId the auto-generated experience ID; must be a positive integer
     */
    public void setExperienceId(int experienceId) {
        this.experienceId = experienceId;
    }

    /**
     * Returns the database primary key for this experience.
     *
     * @return the experience ID, or {@code 0} if not yet persisted
     */
    public int getExperienceId() {
        return experienceId;
    }

    /**
     * Updates the hotel association for this experience.
     *
     * @param hotelId the new hotel ID; must be positive
     */
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * Returns the ID of the hotel that offers this experience.
     *
     * @return hotel ID; always positive for a valid experience
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * Returns the display name of this experience.
     *
     * @return experience name; never {@code null} for a properly constructed instance
     */
    public String getExperienceName() {
        return experienceName;
    }

    /**
     * Updates the display name of this experience.
     *
     * @param experienceName the new name; must not be {@code null} or blank
     */
    public void setExperienceName(String experienceName) {
        this.experienceName = experienceName;
    }

    /**
     * Returns the detailed description of this experience.
     *
     * @return description string; may be empty or {@code null}
     */
    public String getDescription() {
        return description;
    }

    /**
     * Updates the description of this experience.
     *
     * @param description the new description; may be {@code null}
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the reference base cost for this experience.
     *
     * @return base cost in USD; always non-negative
     */
    public double getBaseCost() {
        return baseCost;
    }

    /**
     * Updates the reference base cost for this experience.
     *
     * @param baseCost the new base cost in USD; must be non-negative
     */
    public void setBaseCost(double baseCost) {
        this.baseCost = baseCost;
    }
}
