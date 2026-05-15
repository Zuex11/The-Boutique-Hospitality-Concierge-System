package models;

import java.time.LocalDate;

/**
 * Represents an audit log entry recording a loyalty tier change for a guest
 * in the MIRAGE system.
 *
 * <p>Every time a guest's loyalty tier is updated via
 * {@code GuestDAO.updateLoyalityTier()}, a {@code LoyaltyTierLog} record is
 * automatically written to capture the previous and new tier values, along
 * with the date of the change. This provides a full audit trail of tier
 * progressions for each guest.</p>
 *
 * <p><b>Database mapping:</b> {@code loyalty_tier_log} table.</p>
 */
public class LoyaltyTierLog {

    /** Auto-generated primary key assigned by the database. */
    private int logId;

    /** Foreign key referencing the guest whose tier changed. */
    private int guestId;

    /** The loyalty tier value before the change (e.g. {@code "Silver"}). */
    private String oldTier;

    /** The loyalty tier value after the change (e.g. {@code "Gold"}). */
    private String newTier;

    /**
     * The calendar date on which the tier change occurred.
     * Automatically set to {@link LocalDate#now()} at construction time.
     */
    private LocalDate changedDate;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code LoyaltyTierLog} entry capturing a tier transition.
     * {@code changedDate} is automatically set to today's date.
     * {@code logId} is left at {@code 0} until the record is persisted and
     * the key is set via {@link #setLogId(int)}.
     *
     * @param guestId the ID of the guest whose tier changed; must be positive
     * @param oldTier the previous loyalty tier; e.g. {@code "Standard"}, {@code "Silver"}
     * @param newTier the new loyalty tier; e.g. {@code "Gold"}, {@code "Platinum"}
     */
    public LoyaltyTierLog(int guestId, String oldTier, String newTier) {
        this.guestId = guestId;
        this.oldTier = oldTier;
        this.newTier = newTier;
        this.changedDate = LocalDate.now();
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the database primary key for this log entry.
     *
     * @return the log ID, or {@code 0} if not yet persisted
     */
    public int getLogId() {
        return logId;
    }

    /**
     * Sets the database-generated primary key for this log entry.
     * Called by the DAO layer after a successful {@code INSERT}.
     *
     * @param logId the auto-generated log ID; must be a positive integer
     */
    public void setLogId(int logId) {
        this.logId = logId;
    }

    /**
     * Returns the ID of the guest whose tier was changed.
     *
     * @return guest ID; always positive for a valid log entry
     */
    public int getGuestId() {
        return guestId;
    }

    /**
     * Returns the loyalty tier value before the change.
     *
     * @return old tier string (e.g. {@code "Standard"}); may be {@code "Unknown"}
     *         if the guest's previous tier could not be determined
     */
    public String getOldTier() {
        return oldTier;
    }

    /**
     * Returns the loyalty tier value after the change.
     *
     * @return new tier string (e.g. {@code "Gold"})
     */
    public String getNewTier() {
        return newTier;
    }

    /**
     * Returns the date on which this tier change was recorded.
     *
     * @return the change date as a {@link LocalDate}; never {@code null}
     */
    public LocalDate getChangedDate() {
        return changedDate;
    }
}
