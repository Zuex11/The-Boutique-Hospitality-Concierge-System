package dao;

import db.DatabaseConnection;
import models.Guest;
import models.Reservation;
import models.Suite;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO
{
    private DatabaseConnection db;

    public ReservationDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }
    public void insertReservation(Reservation r) throws SQLException
    {
        String sql = "INSERT INTO reservation (guest_id, suite_id, check_in, check_out) VALUES (?, ?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, r.getGuestId());
        stmt.setInt(2, r.getSuiteId());
        stmt.setDate(3, Date.valueOf(r.getCheckIn()));
        stmt.setDate(4, Date.valueOf(r.getCheckOut()));
        stmt.executeUpdate();
    }
    public void cancelReservation(Reservation r) throws SQLException {
        String sql = "DELETE FROM reservation WHERE reservation_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, r.getReservationId());
        stmt.executeUpdate();
    }
    public List<Reservation> getReservationByGuest(int guestId) throws SQLException {
        String sql = "SELECT * FROM reservation WHERE guest_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        List<Reservation> reservations = new ArrayList<>();
        stmt.setInt(1, guestId);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
        {
            Reservation tempR = new Reservation(rs.getInt("guest_id"),
                    rs.getInt("suite_id"),
                    rs.getDate("check_in").toLocalDate(),
                    rs.getDate("check_out").toLocalDate());
            reservations.add(tempR);
        }
        return reservations;
    }
    public List<Suite> getAvailableSuites() throws SQLException {
    String sql = """
        SELECT * FROM suite
        EXCEPT
        SELECT suite.suite_id, suite.hotel_id, suite.class_id, suite.suite_number
        FROM suite
        INNER JOIN reservation ON suite.suite_id = reservation.suite_id
        WHERE check_out >= GETDATE()
    """;
    List<Suite> list = new ArrayList<>();
    PreparedStatement stmt = db.getConnection().prepareStatement(sql);
    ResultSet rs = stmt.executeQuery();
    while (rs.next()) {
        Suite s = new Suite(
                rs.getInt("hotel_id"),
                rs.getInt("class_id"),
                rs.getString("suite_number")
        );
        list.add(s);
    }
    return list;
}
}