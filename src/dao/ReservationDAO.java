package dao;

import db.DatabaseConnection;
import models.Reservation;
import models.Suite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for all {@link Reservation} and suite-availability
 * operations in the MIRAGE system.
 *
 * <p>This class handles booking new reservations (with overlap checking and
 * automatic cost calculation), cancellation, and various read queries used
 * by the reservation and checkout screens.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Insert reservations with date-overlap validation ({@link #insertReservation(Reservation)})</li>
 *   <li>Cancel reservations and cascade-delete their experiences ({@link #cancelReservation(int)})</li>
 *   <li>Retrieve all reservations ({@link #getAllReservations()})</li>
 *   <li>Retrieve reservations by guest ({@link #getReservationByGuest(int)})</li>
 *   <li>Retrieve available suites with next-available dates ({@link #getAvailableSuites()})</li>
 *   <li>Look up the guest ID for a reservation ({@link #getGuestIdByReservation(int)})</li>
 *   <li>Look up the total room cost for a reservation ({@link #getReservationTotalCost(int)})</li>
 * </ul>
 */
public class ReservationDAO {

    /** Shared database connection obtained from the singleton. */
    private DatabaseConnection db;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a new {@code ReservationDAO} and acquires the shared database connection.
     *
     * @throws SQLException if the database connection cannot be established
     */
    public ReservationDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    // ── INSERT ───────────────────────────────────────────────────────────────

    /**
     * Inserts a new reservation into the {@code reservation} table after
     * validating that the requested suite is not already booked for the
     * specified date range.
     *
     * <h4>Logic flow</h4>
     * <ol>
     *   <li><b>Overlap check:</b> Queries for any non-cancelled reservation on
     *       the same suite whose date range intersects the requested window.
     *       If one is found, a {@link SQLException} is thrown immediately.</li>
     *   <li><b>Cost calculation:</b> Computes the total room cost as
     *       {@code nights × nightly_rate} by calling {@link #getNightlyRate(int)}.</li>
     *   <li><b>Insert:</b> Writes the reservation row with the computed cost.
     *       The database assigns the {@code reservation_id} and sets
     *       {@code status = 'confirmed'} via its default constraint.</li>
     * </ol>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   -- Overlap check
     *   SELECT COUNT(*) FROM reservation
     *   WHERE suite_id = ? AND status != 'cancelled'
     *     AND check_in < ? AND check_out > ?
     *
     *   -- Insert
     *   INSERT INTO reservation (guest_id, suite_id, check_in, check_out, total_cost)
     *   VALUES (?, ?, ?, ?, ?)
     * }</pre>
     *
     * @param r the {@link Reservation} to persist; must have valid
     *          {@code guestId}, {@code suiteId}, {@code checkIn}, and {@code checkOut}
     * @throws SQLException if the suite is already booked for those dates,
     *                      or if any other database error occurs
     */
    public void insertReservation(Reservation r) throws SQLException {
        String overlapCheck = """
            SELECT COUNT(*) FROM reservation
            WHERE suite_id = ?
              AND status != 'cancelled'
              AND check_in  < ?
              AND check_out > ?
        """;
        PreparedStatement check = db.getConnection().prepareStatement(overlapCheck);
        check.setInt(1, r.getSuiteId());
        check.setDate(2, Date.valueOf(r.getCheckOut()));
        check.setDate(3, Date.valueOf(r.getCheckIn()));
        ResultSet rs = check.executeQuery();
        if (rs.next() && rs.getInt(1) > 0)
            throw new SQLException("Suite is already booked for those dates.");

        long nights = java.time.temporal.ChronoUnit.DAYS.between(r.getCheckIn(), r.getCheckOut());
        double rate = getNightlyRate(r.getSuiteId());
        double totalCost = nights * rate;

        String sql = "INSERT INTO reservation (guest_id, suite_id, check_in, check_out, total_cost) VALUES (?, ?, ?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, r.getGuestId());
        stmt.setInt(2, r.getSuiteId());
        stmt.setDate(3, Date.valueOf(r.getCheckIn()));
        stmt.setDate(4, Date.valueOf(r.getCheckOut()));
        stmt.setDouble(5, totalCost);
        stmt.executeUpdate();
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * Cancels a reservation by deleting its experience line items first,
     * then deleting the reservation record itself.
     *
     * <p>The deletion order is intentional: {@code reservation_experience} has a
     * foreign key referencing {@code reservation}, so child rows must be removed
     * before the parent row can be deleted.</p>
     *
     * <p><b>Note:</b> These two deletes are not wrapped in an explicit transaction.
     * If the second delete fails, orphaned {@code reservation_experience} rows
     * will have already been removed.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   DELETE FROM reservation_experience WHERE reservation_id = ?
     *   DELETE FROM reservation WHERE reservation_id = ?
     * }</pre>
     *
     * @param reservationId the ID of the reservation to cancel; must be positive
     * @throws SQLException if the reservation does not exist or a database error occurs
     */
    public void cancelReservation(int reservationId) throws SQLException {
        String deleteExp = "DELETE FROM reservation_experience WHERE reservation_id = ?";
        PreparedStatement s1 = db.getConnection().prepareStatement(deleteExp);
        s1.setInt(1, reservationId);
        s1.executeUpdate();

        String deleteRes = "DELETE FROM reservation WHERE reservation_id = ?";
        PreparedStatement s2 = db.getConnection().prepareStatement(deleteRes);
        s2.setInt(1, reservationId);
        s2.executeUpdate();
    }

    // ── SELECT ───────────────────────────────────────────────────────────────

    /**
     * Retrieves all reservation records from the database.
     *
     * <p>Each row is mapped to a {@link Reservation} instance with
     * {@code reservationId}, {@code status}, and {@code totalCost} populated
     * in addition to the constructor fields.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM reservation
     * }</pre>
     *
     * @return a {@link List} of all {@link Reservation} objects; empty if none exist
     * @throws SQLException if a database error occurs during the query
     */
    public List<Reservation> getAllReservations() throws SQLException {
        String sql = "SELECT * FROM reservation";
        List<Reservation> list = new ArrayList<>();
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Reservation r = new Reservation(
                        rs.getInt("guest_id"),
                        rs.getInt("suite_id"),
                        rs.getDate("check_in").toLocalDate(),
                        rs.getDate("check_out").toLocalDate());
                r.setReservationId(rs.getInt("reservation_id"));
                r.statusSetter(rs.getString("status"));
                r.totalCostSetter(rs.getInt("total_cost"));
                list.add(r);
            }
        }
        return list;
    }

    /**
     * Retrieves all reservations belonging to a specific guest.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM reservation WHERE guest_id = ?
     * }</pre>
     *
     * @param guestId the ID of the guest to query; must be positive
     * @return a {@link List} of {@link Reservation} objects for that guest;
     *         empty if the guest has no reservations
     * @throws SQLException if a database error occurs during the query
     */
    public List<Reservation> getReservationByGuest(int guestId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE guest_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, guestId);
        ResultSet rs = stmt.executeQuery();
        List<Reservation> list = new ArrayList<>();
        while (rs.next()) {
            list.add(new Reservation(
                    rs.getInt("guest_id"),
                    rs.getInt("suite_id"),
                    rs.getDate("check_in").toLocalDate(),
                    rs.getDate("check_out").toLocalDate()));
        }
        return list;
    }

    /**
     * Retrieves all suites along with their next-available date.
     *
     * <p>For each suite, a subquery determines the latest {@code check_out}
     * date among its active (non-cancelled) future reservations. If no such
     * reservation exists, {@code next_available} is {@code null}, meaning
     * the suite is free immediately.</p>
     *
     * <p>Results are ordered by hotel name, then suite number, to support
     * the grouped display in the reservation screen's suite grid.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT s.suite_id, s.suite_number, s.hotel_id, s.class_id,
     *          h.name AS hotel_name,
     *          (SELECT MAX(r.check_out) FROM reservation r
     *           WHERE r.suite_id = s.suite_id
     *             AND r.status != 'cancelled'
     *             AND r.check_out >= GETDATE()) AS next_available
     *   FROM suite s
     *   INNER JOIN hotel h ON s.hotel_id = h.hotel_id
     *   ORDER BY h.name, s.suite_number
     * }</pre>
     *
     * @return a {@link List} of {@link Suite} objects, each enriched with
     *         {@code hotelName} and {@code nextAvailable}; empty if no suites exist
     * @throws SQLException if a database error occurs during the query
     */
    public List<Suite> getAvailableSuites() throws SQLException {
        String sql = """
    SELECT s.suite_id, s.suite_number, s.hotel_id, s.class_id,
           h.name AS hotel_name,
           (SELECT MAX(r.check_out) 
            FROM reservation r 
            WHERE r.suite_id = s.suite_id 
              AND r.status != 'cancelled'
              AND r.check_out >= GETDATE()) AS next_available
    FROM suite s
    INNER JOIN hotel h ON s.hotel_id = h.hotel_id
    ORDER BY h.name, s.suite_number
    """;
        List<Suite> list = new ArrayList<>();
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                Suite s = new Suite(
                        rs.getInt("hotel_id"),
                        rs.getInt("class_id"),
                        rs.getString("suite_number"));
                s.setSuiteId(rs.getInt("suite_id"));
                s.setHotelName(rs.getString("hotel_name"));
                Date na = rs.getDate("next_available");
                s.setNextAvailable(na != null ? na.toLocalDate() : null);
                list.add(s);
            }
        }
        return list;
    }

    /**
     * Looks up the guest ID associated with a given reservation.
     *
     * <p>Used by {@code CheckoutScreen} to identify which guest's
     * {@code total_spend} to update after adding an experience.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT guest_id FROM reservation WHERE reservation_id = ?
     * }</pre>
     *
     * @param reservationId the reservation to look up; must be positive and exist
     * @return the {@code guest_id} linked to this reservation
     * @throws SQLException if the reservation ID does not exist or a database error occurs
     */
    public int getGuestIdByReservation(int reservationId) throws SQLException {
        String sql = "SELECT guest_id FROM reservation WHERE reservation_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, reservationId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getInt("guest_id");
        throw new SQLException("Reservation not found: " + reservationId);
    }

    /**
     * Retrieves the total room cost stored for a given reservation.
     *
     * <p>Used by {@code CheckoutScreen} to display the room charge line item
     * in the guest folio.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT total_cost FROM reservation WHERE reservation_id = ?
     * }</pre>
     *
     * @param reservationId the reservation to look up; must be positive and exist
     * @return the total room cost in USD
     * @throws SQLException if the reservation ID does not exist or a database error occurs
     */
    public double getReservationTotalCost(int reservationId) throws SQLException {
        String sql = "SELECT total_cost FROM reservation WHERE reservation_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, reservationId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getDouble("total_cost");
        throw new SQLException("Reservation not found: " + reservationId);
    }

    // ── Private helpers ──────────────────────────────────────────────────────

    /**
     * Retrieves the nightly rate for a given suite by joining through its suite class.
     *
     * <p>This is a private helper called exclusively by
     * {@link #insertReservation(Reservation)} to compute the total room cost
     * before persisting the reservation.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT sc.nightly_rate FROM suite s
     *   JOIN suite_class sc ON s.class_id = sc.class_id
     *   WHERE s.suite_id = ?
     * }</pre>
     *
     * @param suiteId the ID of the suite to look up; must be positive
     * @return the nightly rate in USD, or {@code 0.0} if the suite is not found
     * @throws SQLException if a database error occurs during the query
     */
    private double getNightlyRate(int suiteId) throws SQLException {
        String sql = """
            SELECT sc.nightly_rate FROM suite s
            JOIN suite_class sc ON s.class_id = sc.class_id
            WHERE s.suite_id = ?
        """;
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, suiteId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getDouble("nightly_rate");
        return 0;
    }
}
