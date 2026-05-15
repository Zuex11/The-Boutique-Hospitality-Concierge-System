package models;

/**
 * Represents a hotel property managed by the MIRAGE system.
 *
 * <p>Each {@code Hotel} has a unique identity, a decorative theme, a physical
 * location, and a declared capacity (total number of suites). Hotels act as
 * the top-level grouping entity — suites, concierge staff, and guest
 * experiences all belong to a specific hotel.</p>
 *
 * <p><b>Database mapping:</b> {@code hotel} table.</p>
 */
public class Hotel {

    /** Auto-generated primary key assigned by the database. */
    private int hotelId;

    /** Display name of the hotel property (e.g. "Velour Cairo"). */
    private String name;

    /** Decorative/design theme of the property (e.g. "Art Deco"). */
    private String theme;

    /** Physical location description (e.g. "Zamalek, Cairo"). */
    private String location;

    /**
     * Declared total number of suites in this property.
     * This is a metadata value set at registration time and does not
     * automatically reflect the number of suite records in the {@code suite} table.
     */
    private int totalSuites;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code Hotel} with all required fields.
     * {@code hotelId} is left at its default ({@code 0}) until the record
     * is persisted and the key is set via {@link #setHotelId(int)}.
     *
     * @param name        the hotel's display name; must not be {@code null} or blank
     * @param theme       the hotel's decorative theme; may be {@code null}
     * @param location    the hotel's physical location; may be {@code null}
     * @param totalSuites the declared total number of suites; must be non-negative
     */
    public Hotel(String name, String theme, String location, int totalSuites) {
        this.name = name;
        this.theme = theme;
        this.location = location;
        this.totalSuites = totalSuites;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the database primary key for this hotel.
     *
     * @return the hotel ID, or {@code 0} if not yet persisted
     */
    public int getHotelId() {
        return hotelId;
    }

    /**
     * Sets the database-generated primary key for this hotel.
     * Called by {@code HotelDAO} after a successful {@code INSERT}.
     *
     * @param hotelId the auto-generated hotel ID; must be a positive integer
     */
    public void setHotelId(int hotelId) {
        this.hotelId = hotelId;
    }

    /**
     * Returns the hotel's display name.
     *
     * @return name string; never {@code null} for a properly constructed instance
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the hotel's display name.
     *
     * @param name the new name; must not be {@code null} or blank
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the hotel's decorative theme.
     *
     * @return theme string, or {@code null} if not specified
     */
    public String getTheme() {
        return theme;
    }

    /**
     * Updates the hotel's decorative theme.
     *
     * @param theme the new theme; may be {@code null}
     */
    public void setTheme(String theme) {
        this.theme = theme;
    }

    /**
     * Returns the hotel's physical location.
     *
     * @return location string, or {@code null} if not specified
     */
    public String getLocation() {
        return location;
    }

    /**
     * Returns the declared total number of suites in this hotel.
     *
     * @return total suite count; always non-negative
     */
    public int getTotalSuites() {
        return totalSuites;
    }

    /**
     * Updates the declared total number of suites.
     *
     * @param totalSuites the new suite count; must be non-negative
     */
    public void setTotalSuites(int totalSuites) {
        this.totalSuites = totalSuites;
    }
}
