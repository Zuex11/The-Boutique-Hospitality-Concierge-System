package dao;

import db.DatabaseConnection;
import models.Guest;
import models.Reservation;
import models.Suite;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReservationDAO {
    private DatabaseConnection db;

    public ReservationDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    public void insertReservation(Reservation r) throws SQLException {
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
        while (rs.next()) {
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
                    rs.getString("suite_number"));
            list.add(s);
        }
        return list;
    }

    public void extendCheckOut(int reservationId, LocalDate newCheckOut) throws SQLException {

        // reject if the new check-out date is in the past
        if (newCheckOut.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("New check-out date cannot be in the past.");
        }

        // SQL query to fetch the current check-out date for the given reservation we are updating
        String getSql = "SELECT check_out FROM reservation WHERE reservation_id = ?";

        // the SELECT statement
        PreparedStatement getStmt = db.getConnection().prepareStatement(getSql);

        // put the reservation ID in the query parameter placeholder
        getStmt.setInt(1, reservationId);

        // execute the query and store the result
        ResultSet rs = getStmt.executeQuery();

        // if no row was returned, the reservation does not exist
        if (!rs.next()) {
            throw new IllegalArgumentException("Reservation not found.");
        }

        // extract the current check-out date from the result set
        LocalDate currentCheckOut = rs.getDate("check_out").toLocalDate();

        // ensure the new date is after the current check-out (must be an
        // actual extension)
        if (!newCheckOut.isAfter(currentCheckOut)) {
            throw new IllegalArgumentException(
                    "New check-out must be later than current check-out: " + currentCheckOut);
        }

        // SQL query to update the check-out date for the given reservation
        String updateSql = "UPDATE reservation SET check_out = ? WHERE reservation_id = ?";

        // Prepare the UPDATE statement
        PreparedStatement updateStmt = db.getConnection().prepareStatement(updateSql);

        // Bind the new check-out date to the first parameter
        updateStmt.setDate(1, Date.valueOf(newCheckOut));

        // Bind the reservation ID to the second parameter
        updateStmt.setInt(2, reservationId);

        // Execute the update
        updateStmt.executeUpdate();
    }
}