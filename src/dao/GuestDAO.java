package dao;

import db.DatabaseConnection;
import models.Guest;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class GuestDAO {
    private final DatabaseConnection db;

    public GuestDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance() ;

    }

    //INSERT a new guest
    public void insertGuest (Guest guest) throws SQLException {
        String sql = "INSERT INTO guest (full_name ,email ,phone ,loyalty_tier) VALUES (?,?,?,?)";

        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setString(1, guest.getFullName());
        stmt.setString(2, guest.getEmail());
        stmt.setString(3, guest.getPhone());
        stmt.setString(4, guest.getLoyaltyTier());

        stmt.executeUpdate();
        

    }

    //update loyalty tier
    public void updateLoyalityTier(int guestId, String newTier) throws SQLException {
        // fetch current tier so we can log it
        String getTier = "SELECT loyalty_tier FROM guest WHERE guest_id = ?";
        PreparedStatement fetch = db.getConnection().prepareStatement(getTier);
        fetch.setInt(1, guestId);
        ResultSet rs = fetch.executeQuery();
        String oldTier = rs.next() ? rs.getString("loyalty_tier") : "Unknown";

        // update the tier
        String update = "UPDATE guest SET loyalty_tier = ? WHERE guest_id = ?";
        PreparedStatement stmt = db.getConnection().prepareStatement(update);
        stmt.setString(1, newTier);
        stmt.setInt(2, guestId);
        stmt.executeUpdate();

        // write the audit log
        String log = "INSERT INTO loyalty_tier_log (guest_id, old_tier, new_tier) VALUES (?, ?, ?)";
        PreparedStatement logStmt = db.getConnection().prepareStatement(log);
        logStmt.setInt(1, guestId);
        logStmt.setString(2, oldTier);
        logStmt.setString(3, newTier);
        logStmt.executeUpdate();
    }

    //select all guests
    public List<Guest> getAllGuests() throws SQLException {
        String sql = "SELECT * FROM guest";

        List <Guest> guests = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet result_set = stmt.executeQuery();

        while(result_set.next()){
            Guest guest = new Guest (
                result_set.getString("full_name"),
                result_set.getString("email"),
                result_set.getString("phone"),
                result_set.getString("loyalty_tier")
                
            );
            guest.setGuestId(result_set.getInt("guest_id"));
            guests.add(guest);
        }

        return guests;

    }

    public void updateTotalSpend(int guestId, double amount) throws SQLException {
        String sql = "UPDATE guest SET total_spend = total_spend + ? WHERE guest_id = ?";

        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setDouble(1, amount);
        stmt.setInt(2, guestId);

        stmt.executeUpdate();
    }

}