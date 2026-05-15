package models;

/**
 * Represents a suite tier / class definition in the MIRAGE system.
 *
 * <p>A {@code SuiteClass} defines the category of a suite — its name
 * (e.g. "Penthouse"), nightly rate, and the amenities that come with it.
 * Multiple individual {@link Suite} records can share the same class,
 * meaning they all have the same rate and amenity set.</p>
 *
 * <p><b>Database mapping:</b> {@code suite_class} table.</p>
 */
public class SuiteClass {

    /** Auto-generated primary key assigned by the database. */
    private int classId;

    /** Display name of this suite tier (e.g. "Garden View", "Penthouse"). */
    private String className;

    /** Nightly rate charged for suites in this class, in USD. */
    private double nightlyRate;

    /**
     * Comma-separated list of amenities included with this suite class
     * (e.g. "Private pool, Butler, Panoramic view").
     */
    private String amenities;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Creates a new {@code SuiteClass} with all required fields.
     * {@code classId} is left at {@code 0} until persisted and the key
     * is set via {@link #setClassId(int)}.
     *
     * @param className   the tier name; must not be {@code null} or blank
     * @param nightlyRate the nightly rate in USD; must be non-negative
     * @param amenities   a description of included amenities; may be {@code null}
     */
    public SuiteClass(String className, double nightlyRate, String amenities) {
        this.className = className;
        this.nightlyRate = nightlyRate;
        this.amenities = amenities;
    }

    // ── Getters & Setters ────────────────────────────────────────────────────

    /**
     * Returns the database primary key for this suite class.
     *
     * @return the class ID, or {@code 0} if not yet persisted
     */
    public int getClassId() {
        return classId;
    }

    /**
     * Sets the database-generated primary key for this suite class.
     * Called by {@code HotelDAO} after a successful {@code INSERT}.
     *
     * @param classId the auto-generated class ID; must be a positive integer
     */
    public void setClassId(int classId) {
        this.classId = classId;
    }

    /**
     * Returns the display name of this suite class.
     *
     * @return class name string; never {@code null} for a properly constructed instance
     */
    public String getClassName() {
        return className;
    }

    /**
     * Updates the display name of this suite class.
     *
     * @param className the new class name; must not be {@code null} or blank
     */
    public void setClassName(String className) {
        this.className = className;
    }

    /**
     * Returns the nightly rate for suites in this class.
     *
     * @return nightly rate in USD; always non-negative
     */
    public double getNightlyRate() {
        return nightlyRate;
    }

    /**
     * Updates the nightly rate for this suite class.
     *
     * @param nightlyRate the new nightly rate in USD; must be non-negative
     */
    public void setNightlyRate(double nightlyRate) {
        this.nightlyRate = nightlyRate;
    }

    /**
     * Returns the amenities description for this suite class.
     *
     * @return amenities string, or {@code null} if not specified
     */
    public String getAmenities() {
        return amenities;
    }

    /**
     * Updates the amenities description.
     *
     * @param amenities the new amenities string; may be {@code null}
     */
    public void setAmenities(String amenities) {
        this.amenities = amenities;
    }
}
