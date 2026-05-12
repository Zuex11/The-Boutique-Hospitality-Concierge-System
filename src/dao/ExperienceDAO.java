package dao;

import db.DatabaseConnection;
import models.ReservationExperience;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExperienceDAO {
    private final DatabaseConnection db;

    public ExperienceDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * Insert a new ReservationExperience (book an experience for a reservation)
     * Covers: 2nd insert requirement
     */
    public void insertExperience(ReservationExperience re) throws SQLException {
        // Validate that experience and concierge belong to the same hotel as the
        // reservation
        String validationSql = """
                    SELECT r.suite_id, s.hotel_id as res_hotel_id,
                           ge.hotel_id as exp_hotel_id, c.hotel_id as con_hotel_id
                    FROM reservation r
                    JOIN suite s ON r.suite_id = s.suite_id
                    JOIN guest_experience ge ON ge.experience_id = ?
                    JOIN concierge c ON c.concierge_id = ?
                    WHERE r.reservation_id = ?
                """;

        try (Connection conn = db.getConnection();
                PreparedStatement validStmt = conn.prepareStatement(validationSql)) {

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
                                "Reservation hotel: " + resHotelId + ", Experience hotel: " + expHotelId +
                                ", Concierge hotel: " + conHotelId);
            }
        }

        // If validation passes, insert the experience
        String sql = "INSERT INTO reservation_experience (reservation_id, experience_id, concierge_id, actual_cost, booked_date) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, re.getReservationId());
            stmt.setInt(2, re.getExperienceId());
            stmt.setInt(3, re.getConciergeId());
            stmt.setDouble(4, re.getActualCost());
            stmt.setDate(5, Date.valueOf(re.getBookedDate()));

            stmt.executeUpdate();
        }
    }

    /**
     * Delete a booked experience from a reservation
     * Covers: 1st delete requirement (with condition on res_exp_id)
     */
    public void deleteExperience(int resExpId) {
        String sql = "DELETE FROM ReservationExperience WHERE res_exp_id = ?";

        try (Connection conn = db.getConnection();
                PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, resExpId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Get all experiences booked for a specific reservation
     * Covers: Select using joins (with GuestExperience and Concierge tables)
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
                        0,                              // hotelId not needed for display
                        rs.getString("experience_name"),
                        "",                             // description not needed for dropdown
                        rs.getDouble("base_cost")
                );
                ge.setExperienceId(rs.getInt("experience_id"));
                experiences.add(ge);
            }
        }

        return experiences;
    }
    public List<ReservationExperience> getExperiencesByReservation(int resId) {
        List<ReservationExperience> experiences = new ArrayList<>();
        String sql = """
                    SELECT re.res_exp_id, re.reservation_id, re.experience_id,
                           re.concierge_id, re.actual_cost, re.booked_date
                    FROM ReservationExperience re
                    JOIN GuestExperience ge ON re.experience_id = ge.experience_id
                    WHERE re.reservation_id = ?
                    ORDER BY re.booked_date DESC
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
