package dao;

import db.DatabaseConnection;
import models.GuestExperience;
import models.ReservationExperience;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for all guest experience operations in the MIRAGE system.
 *
 * <p>This class manages the lifecycle of {@link ReservationExperience} records —
 * the join entity that links a reservation to a booked experience. It also
 * provides queries to discover which experiences are available for a given
 * reservation and to retrieve the full list of experiences on a reservation's folio.</p>
 *
 * <h2>Key constraint enforced here</h2>
 * <p>When booking an experience ({@link #insertExperience(ReservationExperience)}),
 * this DAO validates that the experience <em>and</em> the assigned concierge both
 * belong to the same hotel as the reservation's suite. This cross-entity constraint
 * cannot be expressed as a simple FK and is therefore enforced in application logic
 * using a validation query inside an explicit transaction.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Insert a booked experience with hotel-matching validation
 *       ({@link #insertExperience(ReservationExperience)})</li>
 *   <li>Delete a booked experience by its ID ({@link #deleteExperience(int)})</li>
 *   <li>Retrieve available experiences for a reservation
 *       ({@link #getAvailableExperiencesForReservation(int)})</li>
 *   <li>Retrieve all booked experiences on a reservation's folio
 *       ({@link #getExperiencesByReservation(int)})</li>
 * </ul>
 */
public class ExperienceDAO {

    /** Shared database connection obtained from the singleton. */
    private final DatabaseConnection db;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a new {@code ExperienceDAO} and acquires the shared database connection.
     *
     * @throws SQLException if the database connection cannot be established
     */
    public ExperienceDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    // ── INSERT ───────────────────────────────────────────────────────────────

    /**
     * Books an experience for a reservation inside an explicit transaction,
     * after validating that all three entities (reservation, experience, concierge)
     * belong to the same hotel.
     *
     * <h4>Logic flow</h4>
     * <ol>
     *   <li><b>Disable auto-commit</b> to begin a manual transaction.</li>
     *   <li><b>Validation query:</b> Joins {@code reservation → suite},
     *       {@code guest_experience}, and {@code concierge} to retrieve the
     *       hotel IDs for each. If the join returns no rows, one of the IDs
     *       is invalid.</li>
     *   <li><b>Hotel-match check:</b> Compares the three hotel IDs. If any
     *       differ, the transaction is rolled back and a descriptive
     *       {@link SQLException} is thrown.</li>
     *   <li><b>Insert:</b> Writes the {@code reservation_experience} row.</li>
     *   <li><b>Commit</b> on success; <b>rollback</b> on any failure.</li>
     * </ol>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   -- Validation
     *   SELECT r.suite_id, s.hotel_id AS res_hotel_id,
     *          ge.hotel_id AS exp_hotel_id, c.hotel_id AS con_hotel_id
     *   FROM reservation r
     *   JOIN suite s ON r.suite_id = s.suite_id
     *   JOIN guest_experience ge ON ge.experience_id = ?
     *   JOIN concierge c ON c.concierge_id = ?
     *   WHERE r.reservation_id = ?
     *
     *   -- Insert
     *   INSERT INTO reservation_experience
     *     (reservation_id, experience_id, concierge_id, actual_cost, booked_date)
     *   VALUES (?, ?, ?, ?, ?)
     * }</pre>
     *
     * @param re the {@link ReservationExperience} to persist; must have valid
     *           {@code reservationId}, {@code experienceId}, {@code conciergeId},
     *           {@code actualCost}, and {@code bookedDate}
     * @throws SQLException if any ID is invalid, the hotel-match check fails,
     *                      or a database error occurs — in all cases the transaction
     *                      is rolled back before the exception propagates
     */
    public void insertExperience(ReservationExperience re) throws SQLException {
        String validationSql = """
                    SELECT r.suite_id, s.hotel_id as res_hotel_id,
                           ge.hotel_id as exp_hotel_id, c.hotel_id as con_hotel_id
                    FROM reservation r
                    JOIN suite s ON r.suite_id = s.suite_id
                    JOIN guest_experience ge ON ge.experience_id = ?
                    JOIN concierge c ON c.concierge_id = ?
                    WHERE r.reservation_id = ?
                """;

        String insertSql = "INSERT INTO reservation_experience (reservation_id, experience_id, concierge_id, actual_cost, booked_date) VALUES (?, ?, ?, ?, ?)";

        Connection conn = db.getConnection();
        conn.setAutoCommit(false);

        try {
            PreparedStatement validStmt = conn.prepareStatement(validationSql);
            validStmt.setInt(1, re.getExperienceId());
            validStmt.setInt(2, re.getConciergeId());
            validStmt.setInt(3, re.getReservationId());
            ResultSet rs = validStmt.executeQuery();

            if (!rs.next()) {
                throw new SQLException("Invalid reservation, experience, or concierge ID");
            }

            int resHotelId = rs.getInt("res_hotel_id");
            int expHotelId = rs.getInt("exp_hotel_id");
            int conHotelId = rs.getInt("con_hotel_id");

            if (resHotelId != expHotelId || resHotelId != conHotelId) {
                throw new SQLException(
                        "Experience and concierge must belong to the same hotel as the reservation. " +
                                "Reservation hotel: " + resHotelId +
                                ", Experience hotel: " + expHotelId +
                                ", Concierge hotel: " + conHotelId);
            }

            PreparedStatement stmt = conn.prepareStatement(insertSql);
            stmt.setInt(1, re.getReservationId());
            stmt.setInt(2, re.getExperienceId());
            stmt.setInt(3, re.getConciergeId());
            stmt.setDouble(4, re.getActualCost());
            stmt.setDate(5, Date.valueOf(re.getBookedDate()));
            stmt.executeUpdate();

            conn.commit();

        } catch (SQLException e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
        }
    }

    // ── DELETE ───────────────────────────────────────────────────────────────

    /**
     * Permanently removes a booked experience from a reservation folio
     * by its {@code res_exp_id} primary key.
     *
     * <p>This operation is irreversible. The UI displays a warning pill
     * before allowing this action.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   DELETE FROM reservation_experience WHERE res_exp_id = ?
     * }</pre>
     *
     * @param resExpId the primary key of the {@code reservation_experience} row to delete;
     *                 must be positive
     * @throws SQLException if a database error occurs during deletion
     */
    public void deleteExperience(int resExpId) throws SQLException {
        String sql = "DELETE FROM reservation_experience WHERE res_exp_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, resExpId);
        stmt.executeUpdate();
    }

    // ── SELECT ───────────────────────────────────────────────────────────────

    /**
     * Retrieves all guest experiences that are eligible to be booked for a given
     * reservation — i.e. experiences offered by the same hotel as the reservation's suite.
     *
     * <p>The result is used to populate the experience dropdown in the
     * {@code CheckoutScreen}. Only {@code experience_id}, {@code experience_name},
     * and {@code base_cost} are fetched; {@code description} and {@code hotelId}
     * are left empty as they are not needed for the dropdown.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT ge.experience_id, ge.experience_name, ge.base_cost
     *   FROM guest_experience ge
     *   WHERE ge.hotel_id = (
     *       SELECT s.hotel_id FROM reservation r
     *       JOIN suite s ON r.suite_id = s.suite_id
     *       WHERE r.reservation_id = ?
     *   )
     * }</pre>
     *
     * @param reservationId the ID of the reservation to find eligible experiences for;
     *                      must be positive and exist in the database
     * @return a {@link List} of {@link GuestExperience} objects available at the
     *         relevant hotel; empty if no experiences are defined for that hotel
     * @throws SQLException if the reservation ID is invalid or a database error occurs
     */
    public List<GuestExperience> getAvailableExperiencesForReservation(int reservationId) throws SQLException {
        String sql = """
                    SELECT ge.experience_id, ge.experience_name, ge.base_cost
                    FROM guest_experience ge
                    WHERE ge.hotel_id = (
                        SELECT s.hotel_id
                        FROM reservation r
                        JOIN suite s ON r.suite_id = s.suite_id
                        WHERE r.reservation_id = ?
                    )
                """;

        List<GuestExperience> experiences = new ArrayList<>();
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                GuestExperience ge = new GuestExperience(
                        0,
                        rs.getString("experience_name"),
                        "",
                        rs.getDouble("base_cost"));
                ge.setExperienceId(rs.getInt("experience_id"));
                experiences.add(ge);
            }
        }
        return experiences;
    }

    /**
     * Retrieves all experience line items booked under a specific reservation,
     * ordered by booking date descending (most recent first).
     *
     * <p>Used by {@code CheckoutScreen} to render the itemised folio view.
     * Errors are swallowed and printed to stderr rather than propagated, so
     * the folio simply renders empty if the query fails.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT res_exp_id, reservation_id, experience_id,
     *          concierge_id, actual_cost, booked_date
     *   FROM reservation_experience
     *   WHERE reservation_id = ?
     *   ORDER BY booked_date DESC
     * }</pre>
     *
     * @param resId the ID of the reservation whose folio to retrieve; must be positive
     * @return a {@link List} of {@link ReservationExperience} objects, newest first;
     *         empty if no experiences have been booked or if the query fails
     */
    public List<ReservationExperience> getExperiencesByReservation(int resId) {
        List<ReservationExperience> experiences = new ArrayList<>();
        String sql = """
                    SELECT res_exp_id, reservation_id, experience_id,
                           concierge_id, actual_cost, booked_date
                    FROM reservation_experience
                    WHERE reservation_id = ?
                    ORDER BY booked_date DESC
                """;

        try (Connection conn = db.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, resId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                ReservationExperience re = new ReservationExperience(
                        rs.getInt("reservation_id"),
                        rs.getInt("experience_id"),
                        rs.getInt("concierge_id"),
                        rs.getDouble("actual_cost"),
                        rs.getDate("booked_date").toLocalDate());
                re.setResExpId(rs.getInt("res_exp_id"));
                experiences.add(re);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return experiences;
    }
}
