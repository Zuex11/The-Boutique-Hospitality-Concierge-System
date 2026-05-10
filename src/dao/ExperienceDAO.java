package dao;

import db.DatabaseConnection;
import models.GuestExperience;
import models.ReservationExperience;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExperienceDAO {
    private final DatabaseConnection dbConnection;

    public ExperienceDAO(DatabaseConnection dbConnection) {
        this.dbConnection = dbConnection;
    }

    // INSERT: Add a new guest experience (covers 1st insert on Experience table)
    public boolean addExperience(GuestExperience experience) {
        String sql = "INSERT INTO GuestExperience (experience_name, description, base_cost) VALUES (?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setString(1, experience.getExperienceName());
            stmt.setString(2, experience.getDescription());
            stmt.setBigDecimal(3, experience.getBaseCost());
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error adding experience: " + e.getMessage());
            return false;
        }
    }

    // INSERT: Book an experience for a reservation (covers 2nd insert on ReservationExperience table)
    public boolean bookReservationExperience(ReservationExperience resExp) {
        String sql = "INSERT INTO ReservationExperience (reservation_id, experience_id, concierge_id, actual_cost, booked_date) VALUES (?, ?, ?, ?, ?)";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, resExp.getReservationId());
            stmt.setInt(2, resExp.getExperienceId());
            stmt.setInt(3, resExp.getConciergeId());
            stmt.setBigDecimal(4, resExp.getActualCost());
            stmt.setDate(5, Date.valueOf(resExp.getBookedDate()));
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error booking reservation experience: " + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove a guest experience from catalog
    public boolean deleteExperience(int experienceId) {
        String sql = "DELETE FROM GuestExperience WHERE experience_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, experienceId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error deleting experience: " + e.getMessage());
            return false;
        }
    }

    // DELETE: Remove a booked experience from a reservation (covers 2nd delete)
    public boolean cancelReservationExperience(int resExpId) {
        String sql = "DELETE FROM ReservationExperience WHERE res_exp_id = ?";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, resExpId);
            
            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
            
        } catch (SQLException e) {
            System.err.println("Error canceling reservation experience: " + e.getMessage());
            return false;
        }
    }

    // SELECT: Get all available guest experiences
    public List<GuestExperience> getAllExperiences() {
        List<GuestExperience> experiences = new ArrayList<>();
        String sql = "SELECT * FROM GuestExperience ORDER BY experience_name";
        
        try (Connection conn = dbConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            
            while (rs.next()) {
                GuestExperience experience = new GuestExperience(
                    rs.getInt("experience_id"),
                    rs.getString("experience_name"),
                    rs.getString("description"),
                    rs.getBigDecimal("base_cost")
                );
                experiences.add(experience);
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching experiences: " + e.getMessage());
        }
        
        return experiences;
    }

    // SELECT JOIN: Get reservation experiences with details (for checkout/folio view)
    public List<String> getReservationExperiencesWithDetails(int reservationId) {
        List<String> folioItems = new ArrayList<>();
        String sql = "SELECT re.res_exp_id, ge.experience_name, re.actual_cost, " +
                     "re.booked_date, c.full_name as concierge_name " +
                     "FROM ReservationExperience re " +
                     "JOIN GuestExperience ge ON re.experience_id = ge.experience_id " +
                     "JOIN Concierge c ON re.concierge_id = c.concierge_id " +
                     "WHERE re.reservation_id = ? " +
                     "ORDER BY re.booked_date";
        
        try (Connection conn = dbConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            
            stmt.setInt(1, reservationId);
            
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String item = String.format("ID: %d | %s | Cost: $%.2f | Date: %s | Concierge: %s",
                        rs.getInt("res_exp_id"),
                        rs.getString("experience_name"),
                        rs.getDouble("actual_cost"),
                        rs.getDate("booked_date").toLocalDate(),
                        rs.getString("concierge_name")
                    );
                    folioItems.add(item);
                }
            }
            
        } catch (SQLException e) {
            System.err.println("Error fetching reservation experiences: " + e.getMessage());
        }
        
        return folioItems;
    }
}
