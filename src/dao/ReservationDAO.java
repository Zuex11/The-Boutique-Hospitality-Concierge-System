package dao;

import db.DatabaseConnection;
import models.Reservation;
import models.Suite;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private DatabaseConnection db;

    public ReservationDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

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

    public int getGuestIdByReservation(int reservationId) throws SQLException {
        String sql = "SELECT guest_id FROM reservation WHERE reservation_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, reservationId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getInt("guest_id");
        throw new SQLException("Reservation not found: " + reservationId);
    }

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


    public double getReservationTotalCost(int reservationId) throws SQLException {
        String sql = "SELECT total_cost FROM reservation WHERE reservation_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, reservationId);
        ResultSet rs = stmt.executeQuery();
        if (rs.next()) return rs.getDouble("total_cost");
        throw new SQLException("Reservation not found: " + reservationId);
    }
}