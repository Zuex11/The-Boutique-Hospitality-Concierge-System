package dao;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for all analytics and reporting queries in the
 * MIRAGE system.
 *
 * <p>This class provides read-only queries that aggregate data across multiple
 * tables to produce the six management reports displayed on the
 * {@code ReportScreen}. All reports are scoped to the <b>previous calendar
 * month</b> unless otherwise noted, computed dynamically using SQL Server's
 * {@code DATEADD} and {@code GETDATE()} functions — no hardcoded dates.</p>
 *
 * <p>All methods in this class are read-only ({@code SELECT} only).
 * No data is mutated by any report query.</p>
 *
 * <h2>Reports provided</h2>
 * <ol>
 *   <li>{@link #getMostPopularSuiteClass()} — Suite class with the most reservations overall</li>
 *   <li>{@link #getHotelsWithNoExperiencesLastMonth()} — Hotels with zero experience bookings last month</li>
 *   <li>{@link #getTopConciergeLastMonth()} — Concierge with highest total experience value last month</li>
 *   <li>{@link #getGuestsWithNoExperiencesLastMonth()} — Guests who reserved but booked no experiences last month</li>
 *   <li>{@link #getAvailableSuitesPerHotelLastMonth()} — Suites not reserved at any point last month</li>
 *   <li>{@link #getGuestSpendLastMonth()} — Each checked-out guest's total suite spend last month</li>
 * </ol>
 */
public class ReportsDAO {

    /** Shared database connection obtained from the singleton. */
    private final DatabaseConnection db;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a new {@code ReportsDAO} and acquires the shared database connection.
     *
     * @throws SQLException if the database connection cannot be established
     */
    public ReportsDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    // ── Report 1 ─────────────────────────────────────────────────────────────

    /**
     * Returns the suite class with the highest total number of reservations
     * across all time (not limited to last month).
     *
     * <p>The result is formatted as a single human-readable string:
     * {@code "<class_name> — <count> reservations"}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT TOP 1 sc.class_name, COUNT(r.reservation_id) AS total
     *   FROM reservation r
     *   JOIN suite s ON r.suite_id = s.suite_id
     *   JOIN suite_class sc ON s.class_id = sc.class_id
     *   GROUP BY sc.class_name
     *   ORDER BY total DESC
     * }</pre>
     *
     * @return a formatted string with the most popular suite class and its
     *         reservation count; {@code "No data."} if the table is empty
     * @throws SQLException if a database error occurs during the query
     */
    public String getMostPopularSuiteClass() throws SQLException {
        String sql = """
            SELECT TOP 1 sc.class_name, COUNT(r.reservation_id) AS total
            FROM reservation r
            JOIN suite s ON r.suite_id = s.suite_id
            JOIN suite_class sc ON s.class_id = sc.class_id
            GROUP BY sc.class_name
            ORDER BY total DESC
        """;
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getString("class_name") + " — " + rs.getInt("total") + " reservations";
        return "No data.";
    }

    // ── Report 2 ─────────────────────────────────────────────────────────────

    /**
     * Returns the names of all hotels that had zero guest experience bookings
     * during the previous calendar month.
     *
     * <p>A hotel is considered to have had an experience if any
     * {@code reservation_experience} row — joined back through
     * {@code reservation → suite → hotel} — has a {@code booked_date}
     * falling within the previous calendar month. Hotels not appearing in
     * that set are included in this report.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT h.name FROM hotel h
     *   WHERE h.hotel_id NOT IN (
     *       SELECT s.hotel_id
     *       FROM reservation_experience re
     *       JOIN reservation r ON re.reservation_id = r.reservation_id
     *       JOIN suite s ON r.suite_id = s.suite_id
     *       WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
     *         AND YEAR(re.booked_date)  = YEAR(DATEADD(MONTH, -1, GETDATE()))
     *   )
     * }</pre>
     *
     * @return a {@link List} of hotel name strings with no experience activity
     *         last month; contains {@code "All hotels had experiences last month."}
     *         if the list would otherwise be empty
     * @throws SQLException if a database error occurs during the query
     */
    public List<String> getHotelsWithNoExperiencesLastMonth() throws SQLException {
        String sql = """
            SELECT h.name
            FROM hotel h
            WHERE h.hotel_id NOT IN (
                SELECT s.hotel_id
                FROM reservation_experience re
                JOIN reservation r ON re.reservation_id = r.reservation_id
                JOIN suite s ON r.suite_id = s.suite_id
                WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("name"));
        if (results.isEmpty()) results.add("All hotels had experiences last month.");
        return results;
    }

    // ── Report 3 ─────────────────────────────────────────────────────────────

    /**
     * Returns the concierge who managed the highest total value of experience
     * bookings during the previous calendar month.
     *
     * <p>Total value is computed as the sum of {@code actual_cost} across all
     * {@code reservation_experience} rows handled by each concierge in that month.
     * Only the top-ranked concierge is returned.</p>
     *
     * <p>The result is formatted as:
     * {@code "<full_name> — $<total_value>"}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT TOP 1 c.full_name, SUM(re.actual_cost) AS total_value
     *   FROM reservation_experience re
     *   JOIN concierge c ON re.concierge_id = c.concierge_id
     *   WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
     *     AND YEAR(re.booked_date)  = YEAR(DATEADD(MONTH, -1, GETDATE()))
     *   GROUP BY c.full_name
     *   ORDER BY total_value DESC
     * }</pre>
     *
     * @return a formatted string naming the top concierge and their total value;
     *         {@code "No data."} if no experience bookings exist for last month
     * @throws SQLException if a database error occurs during the query
     */
    public String getTopConciergeLastMonth() throws SQLException {
        String sql = """
            SELECT TOP 1 c.full_name, SUM(re.actual_cost) AS total_value
            FROM reservation_experience re
            JOIN concierge c ON re.concierge_id = c.concierge_id
            WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
              AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            GROUP BY c.full_name
            ORDER BY total_value DESC
        """;
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getString("full_name") + " — $" + String.format("%.2f", rs.getDouble("total_value"));
        return "No data.";
    }

    // ── Report 4 ─────────────────────────────────────────────────────────────

    /**
     * Returns guests who made at least one reservation last month but did not
     * book any add-on experiences during that same month.
     *
     * <p>The query uses two subqueries:</p>
     * <ul>
     *   <li>An IN subquery to find guests with a check-in last month.</li>
     *   <li>A NOT IN subquery to exclude guests who appear in
     *       {@code reservation_experience} with a booking date last month.</li>
     * </ul>
     *
     * <p>Each result is formatted as: {@code "<full_name> | <email>"}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT g.full_name, g.email FROM guest g
     *   WHERE g.guest_id IN (
     *       SELECT r.guest_id FROM reservation r
     *       WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
     *         AND YEAR(r.check_in)  = YEAR(DATEADD(MONTH, -1, GETDATE()))
     *   )
     *   AND g.guest_id NOT IN (
     *       SELECT r.guest_id
     *       FROM reservation_experience re
     *       JOIN reservation r ON re.reservation_id = r.reservation_id
     *       WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
     *         AND YEAR(re.booked_date)  = YEAR(DATEADD(MONTH, -1, GETDATE()))
     *   )
     * }</pre>
     *
     * @return a {@link List} of formatted strings, one per guest;
     *         contains {@code "No guests without experiences last month."}
     *         if all guests booked at least one experience
     * @throws SQLException if a database error occurs during the query
     */
    public List<String> getGuestsWithNoExperiencesLastMonth() throws SQLException {
        String sql = """
            SELECT g.full_name, g.email
            FROM guest g
            WHERE g.guest_id IN (
                SELECT r.guest_id
                FROM reservation r
                WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(r.check_in) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
            AND g.guest_id NOT IN (
                SELECT r.guest_id
                FROM reservation_experience re
                JOIN reservation r ON re.reservation_id = r.reservation_id
                WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("full_name") + " | " + rs.getString("email"));
        if (results.isEmpty()) results.add("No guests without experiences last month.");
        return results;
    }

    // ── Report 5 ─────────────────────────────────────────────────────────────

    /**
     * Returns all suites that were not reserved at any point during the previous
     * calendar month, grouped by hotel.
     *
     * <p>A suite is considered reserved for the month if any active reservation
     * overlaps the month window — i.e. its {@code check_in} is on or before the
     * last day of last month AND its {@code check_out} is on or after the first
     * day of last month. Suites not meeting this criterion are returned.</p>
     *
     * <p>Results are ordered by hotel name, then suite number.</p>
     *
     * <p>Each result is formatted as: {@code "<hotel_name> — Suite <suite_number>"}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT h.name AS hotel_name, s.suite_number
     *   FROM suite s
     *   JOIN hotel h ON s.hotel_id = h.hotel_id
     *   WHERE s.suite_id NOT IN (
     *       SELECT r.suite_id FROM reservation r
     *       WHERE r.check_in  <= EOMONTH(DATEADD(MONTH, -1, GETDATE()))
     *         AND r.check_out >= DATEFROMPARTS(
     *               YEAR(DATEADD(MONTH,-1,GETDATE())),
     *               MONTH(DATEADD(MONTH,-1,GETDATE())), 1)
     *   )
     *   ORDER BY h.name, s.suite_number
     * }</pre>
     *
     * @return a {@link List} of formatted strings, one per available suite;
     *         contains {@code "No available suites last month."} if all suites
     *         were reserved at some point during the month
     * @throws SQLException if a database error occurs during the query
     */
    public List<String> getAvailableSuitesPerHotelLastMonth() throws SQLException {
        String sql = """
            SELECT h.name AS hotel_name, s.suite_number
            FROM suite s
            JOIN hotel h ON s.hotel_id = h.hotel_id
            WHERE s.suite_id NOT IN (
                SELECT r.suite_id
                FROM reservation r
                WHERE r.check_in <= EOMONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND r.check_out >= DATEFROMPARTS(
                        YEAR(DATEADD(MONTH, -1, GETDATE())),
                        MONTH(DATEADD(MONTH, -1, GETDATE())),
                        1)
            )
            ORDER BY h.name, s.suite_number
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("hotel_name") + " — Suite " + rs.getString("suite_number"));
        if (results.isEmpty()) results.add("No available suites last month.");
        return results;
    }

    // ── Report 6 ─────────────────────────────────────────────────────────────

    /**
     * Returns each guest's profile alongside their total suite spending for
     * the previous calendar month, limited to reservations with a
     * {@code status} of {@code 'checked-out'}.
     *
     * <p>Results are ordered by total spend descending (highest spender first),
     * making it easy to identify top-value guests at a glance.</p>
     *
     * <p>Each result is formatted as:
     * {@code "<full_name> | <email> | <loyalty_tier> | $<spent>"}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT g.full_name, g.email, g.loyalty_tier, SUM(r.total_cost) AS spent
     *   FROM guest g
     *   JOIN reservation r ON g.guest_id = r.guest_id
     *   WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
     *     AND YEAR(r.check_in)  = YEAR(DATEADD(MONTH, -1, GETDATE()))
     *     AND r.status = 'checked-out'
     *   GROUP BY g.full_name, g.email, g.loyalty_tier
     *   ORDER BY spent DESC
     * }</pre>
     *
     * @return a {@link List} of formatted strings, one per guest, ordered by
     *         spend descending; contains {@code "No checkout data last month."}
     *         if no checked-out reservations exist for the previous month
     * @throws SQLException if a database error occurs during the query
     */
    public List<String> getGuestSpendLastMonth() throws SQLException {
        String sql = """
            SELECT g.full_name, g.email, g.loyalty_tier, SUM(r.total_cost) AS spent
            FROM guest g
            JOIN reservation r ON g.guest_id = r.guest_id
            WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
              AND YEAR(r.check_in) = YEAR(DATEADD(MONTH, -1, GETDATE()))
              AND r.status = 'checked-out'
            GROUP BY g.full_name, g.email, g.loyalty_tier
            ORDER BY spent DESC
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("full_name") + " | " +
                        rs.getString("email") + " | " +
                        rs.getString("loyalty_tier") + " | $" +
                        String.format("%.2f", rs.getDouble("spent")));
        if (results.isEmpty()) results.add("No checkout data last month.");
        return results;
    }
}
