package models;

import java.time.LocalDate;

/**
 * Represents a booked experience attached to a specific reservation in the MIRAGE system.
 *
 * <p>A {@code ReservationExperience} is the join entity between a
 * {@link Reservation} and a {@link GuestExperience}. It records which
 * experience was booked, which concierge handled it, the actual amount
 * charged (which may differ from the experience's base cost), and the
 * date it was booked.</p>
 *
 * <p><b>Constraints enforced at insert time by {@code ExperienceDAO}:</b></p>
 * <ul>
 *   <li>The experience must belong to the same hotel as the reservation's suite.</li>
 *   <li>The concierge must also belong to that same hotel.</li>
 * </ul>
 *
 * <p><b>Database mapping:</b> {@code reservation_experience} table.</p>
 */
public class ReservationExperience {

    /** Auto-generated primary key assigned by the database. */
    private int resExpId;

    /** Foreign key referencing the parent {@link Reservation}. */
    private int reservationId;

    /** Foreign key referencing the {@link GuestExperience} that was booked. */
    private int experienceId;

    /**
     * Foreign key referencing the {@link Concierge} who managed this experience.
     * Note: the field is named {@code coniergeId} (typo preserved from original source).
     */
    private int coniergeId;

    /**
     * The actual amount charged to the guest's folio for this experience, in USD.
     * May differ from {@code GuestExperience.baseCost}.
     */
    private double actualCost;

    /** The calendar date on which this experience was booked. */
    private LocalDate bookedDate;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code ReservationExperience} linking a reservation to an
     * experience, with the associated concierge and billing details.
     * {@code resExpId} is left at {@code 0} until persisted and the key is
     * set via {@link #setResExpId(int)}.
     *
     * @param reservationId the ID of the parent reservation; must be positive
     * @param experienceId  the ID of the experience being booked; must be positive
     * @param coniergeId    the ID of the concierge managing this experience; must be positive
     * @param actualCost    the actual amount to charge in USD; must be non-negative
     * @param bookedDate    the date the experience was booked; must not be {@code null}
     */
    public ReservationExperience(int reservationId, int experienceId, int coniergeId,
                                  double actualCost, LocalDate bookedDate) {
        this.reservationId = reservationId;
        this.experienceId = experienceId;
        this.coniergeId = coniergeId;
        this.actualCost = actualCost;
        this.bookedDate = bookedDate;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the database primary key for this reservation-experience record.
     *
     * @return the res-experience ID, or {@code 0} if not yet persisted
     */
    public int getResExpId() {
        return resExpId;
    }

    /**
     * Sets the database-generated primary key for this record.
     * Called by {@code ExperienceDAO} after a successful {@code INSERT}.
     *
     * @param resExpId the auto-generated res-experience ID; must be a positive integer
     */
    public void setResExpId(int resExpId) {
        this.resExpId = resExpId;
    }

    /**
     * Returns the ID of the parent reservation.
     *
     * @return reservation ID; always positive for a valid record
     */
    public int getReservationId() {
        return reservationId;
    }

    /**
     * Returns the ID of the experience that was booked.
     *
     * @return experience ID; always positive for a valid record
     */
    public int getExperienceId() {
        return experienceId;
    }

    /**
     * Returns the ID of the concierge who managed this experience.
     *
     * @return concierge ID; always positive for a valid record
     */
    public int getConciergeId() {
        return coniergeId;
    }

    /**
     * Returns the actual amount charged for this experience.
     *
     * @return actual cost in USD; always non-negative
     */
    public double getActualCost() {
        return actualCost;
    }

    /**
     * Updates the actual cost charged for this experience.
     *
     * @param actualCost the new cost in USD; must be non-negative
     */
    public void setActualCost(double actualCost) {
        this.actualCost = actualCost;
    }

    /**
     * Returns the date this experience was booked.
     *
     * @return booking date as a {@link LocalDate}; never {@code null} for a valid record
     */
    public LocalDate getBookedDate() {
        return bookedDate;
    }
}
