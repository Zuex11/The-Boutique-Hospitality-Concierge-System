package models;

/**
 * Represents a hotel guest registered in the MIRAGE system.
 *
 * <p>A {@code Guest} holds personal contact information, a loyalty tier,
 * and a running total of all spending accumulated across reservations and
 * add-on experiences. Instances are created by the application when
 * registering new guests and are hydrated from the database by
 * {@code GuestDAO}.</p>
 *
 * <p><b>Database mapping:</b> {@code guest} table.</p>
 */
public class Guest {

    /** Auto-generated primary key assigned by the database. */
    private int guestId;

    /**
     * Cumulative amount (in USD) the guest has spent on experiences.
     * Updated incrementally via {@code GuestDAO.updateTotalSpend()}.
     */
    private int totalSpend;

    /** Full legal name of the guest. */
    private String fullName;

    /** Unique e-mail address used as a contact identifier. */
    private String email;

    /** Contact phone number (free-form, e.g. "+20 10..."). */
    private String phone;

    /**
     * Loyalty programme tier. Valid values (enforced by the UI):
     * {@code "Standard"}, {@code "Silver"}, {@code "Gold"}, {@code "Platinum"}.
     */
    private String loyaltyTier;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code Guest} with all required profile fields.
     * {@code guestId} is left at its default ({@code 0}) until the record
     * is persisted and the generated key is set via {@link #setGuestId(int)}.
     *
     * @param fullName    the guest's full name; must not be {@code null} or blank
     * @param email       the guest's e-mail address; must not be {@code null} or blank
     * @param phone       the guest's phone number; may be {@code null}
     * @param loyaltyTier the initial loyalty tier; must be one of the accepted values
     */
    public Guest(String fullName, String email, String phone, String loyaltyTier) {
        this.fullName = fullName;
        this.email = email;
        this.phone = phone;
        this.loyaltyTier = loyaltyTier;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Sets the database-generated primary key for this guest.
     * Called by {@code GuestDAO} after a successful {@code INSERT}.
     *
     * @param id the auto-generated guest ID; must be a positive integer
     */
    public void setGuestId(int id) {
        this.guestId = id;
    }

    /**
     * Returns the database primary key for this guest.
     *
     * @return the guest ID, or {@code 0} if the guest has not yet been persisted
     */
    public int getGuestId() {
        return guestId;
    }

    /**
     * Returns the guest's full name.
     *
     * @return full name string; never {@code null} for a properly constructed instance
     */
    public String getFullName() {
        return fullName;
    }

    /**
     * Updates the guest's full name.
     *
     * @param fullName the new full name; must not be {@code null} or blank
     */
    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    /**
     * Returns the guest's phone number.
     *
     * @return phone string, or {@code null} if not provided
     */
    public String getPhone() {
        return phone;
    }

    /**
     * Updates the guest's phone number.
     *
     * @param phone the new phone number; may be {@code null}
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * Returns the guest's e-mail address.
     *
     * @return e-mail string; never {@code null} for a properly constructed instance
     */
    public String getEmail() {
        return email;
    }

    /**
     * Updates the guest's e-mail address.
     *
     * @param email the new e-mail; must not be {@code null} or blank
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the guest's current loyalty tier.
     *
     * @return one of {@code "Standard"}, {@code "Silver"}, {@code "Gold"},
     *         or {@code "Platinum"}
     */
    public String getLoyaltyTier() {
        return loyaltyTier;
    }

    /**
     * Updates the guest's loyalty tier.
     * Note: tier changes made through {@code GuestDAO.updateLoyalityTier()}
     * are automatically audit-logged in the {@code loyalty_tier_log} table.
     *
     * @param loyaltyTier the new tier; must be one of the accepted values
     */
    public void setLoyaltyTier(String loyaltyTier) {
        this.loyaltyTier = loyaltyTier;
    }

    /**
     * Overwrites the in-memory total-spend value.
     * Prefer using {@code GuestDAO.updateTotalSpend()} to persist changes.
     *
     * @param spends the new total spend amount in USD
     */
    public void setTotalSpend(int spends) {
        this.totalSpend = spends;
    }

    /**
     * Returns the cumulative amount the guest has spent on experiences.
     *
     * @return total spend in USD as an integer
     */
    public int getTotalSpend() {
        return this.totalSpend;
    }
}
