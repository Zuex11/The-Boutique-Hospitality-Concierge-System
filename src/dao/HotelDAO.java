package dao;

import db.DatabaseConnection;
import models.GuestExperience;
import models.Hotel;
import models.SuiteClass;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {
    private final DatabaseConnection db;

    public HotelDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    /**
     * Insert a new hotel
     */
    public void insertHotel(Hotel hotel) throws SQLException {
        String sql = "INSERT INTO hotel (name, theme, location, total_suites) VALUES (?, ?, ?, ?)";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setString(1, hotel.getName());
            stmt.setString(2, hotel.getTheme());
            stmt.setString(3, hotel.getLocation());
            stmt.setInt(4, hotel.getTotalSuites());
            
            stmt.executeUpdate();
            
            try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                if (generatedKeys.next()) {
                    // Note: You'll need to add a setHotelId method to your Hotel model
                    // hotel.setHotelId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Update hotel information
     */
    public void updateHotel(Hotel hotel) throws SQLException {
        String sql = "UPDATE hotel SET name = ?, theme = ?, location = ?, total_suites = ? WHERE hotel_id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, hotel.getName());
            stmt.setString(2, hotel.getTheme());
            stmt.setString(3, hotel.getLocation());
            stmt.setInt(4, hotel.getTotalSuites());
            stmt.setInt(5, hotel.getHotelId());
            
            stmt.executeUpdate();
        }
    }

    /**
     * Delete a hotel by ID
     */
    public void deleteHotel(int hotelId) throws SQLException {
        String sql = "DELETE FROM hotel WHERE hotel_id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            stmt.executeUpdate();
        }
    }

    /**
     * Get hotel by ID
     */
    public Hotel getHotelById(int hotelId) throws SQLException {
        String sql = "SELECT * FROM hotel WHERE hotel_id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getString("name"),
                    rs.getString("theme"),
                    rs.getString("location"),
                    rs.getInt("total_suites")
                );
                // Note: You'll need to add a setHotelId method to your Hotel model
                // hotel.setHotelId(rs.getInt("hotel_id"));
                return hotel;
            }
            return null;
        }
    }

    /**
     * Get all hotels
     */
    public List<Hotel> getAllHotels() throws SQLException {
        String sql = "SELECT * FROM hotel ORDER BY hotel_id";
        List<Hotel> hotels = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getString("name"),
                    rs.getString("theme"),
                    rs.getString("location"),
                    rs.getInt("total_suites")
                );
                // hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    /**
     * Get hotels by location
     */
    public List<Hotel> getHotelsByLocation(String location) throws SQLException {
        String sql = "SELECT * FROM hotel WHERE location LIKE ? ORDER BY name";
        List<Hotel> hotels = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, "%" + location + "%");
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getString("name"),
                    rs.getString("theme"),
                    rs.getString("location"),
                    rs.getInt("total_suites")
                );
                // hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    /**
     * Get hotels by theme
     */
    public List<Hotel> getHotelsByTheme(String theme) throws SQLException {
        String sql = "SELECT * FROM hotel WHERE theme = ? ORDER BY name";
        List<Hotel> hotels = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setString(1, theme);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getString("name"),
                    rs.getString("theme"),
                    rs.getString("location"),
                    rs.getInt("total_suites")
                );
                // hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    /**
     * Get suite classes for a specific hotel (using join)
     * Covers: Select using joins between hotel and suite_class
     */
    public List<SuiteClass> getHotelSuiteClasses(int hotelId) throws SQLException {
        String sql = """
            SELECT sc.class_id, sc.hotel_id, sc.class_name, sc.nightly_rate, sc.amenities
            FROM suite_class sc
            WHERE sc.hotel_id = ?
            ORDER BY sc.nightly_rate
        """;
        
        List<SuiteClass> suiteClasses = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SuiteClass suiteClass = new SuiteClass(
                    rs.getInt("hotel_id"),
                    rs.getString("class_name"),
                    rs.getDouble("nightly_rate"),
                    rs.getString("amenities")
                );
                suiteClass.setClassId(rs.getInt("class_id"));
                suiteClasses.add(suiteClass);
            }
        }
        return suiteClasses;
    }

    /**
     * Get total number of suites across all hotels (aggregate function)
     * Covers: Aggregate function (SUM)
     */
    public int getTotalSuitesAcrossAllHotels() throws SQLException {
        String sql = "SELECT SUM(total_suites) as total_suites FROM hotel";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            if (rs.next()) {
                return rs.getInt("total_suites");
            }
            return 0;
        }
    }

    /**
     * Get hotel count by location (aggregate with GROUP BY)
     * Covers: GROUP BY clause
     */
    public List<Object[]> getHotelCountByLocation() throws SQLException {
        String sql = """
            SELECT location, COUNT(*) as hotel_count, SUM(total_suites) as total_suites
            FROM hotel
            GROUP BY location
            ORDER BY location
        """;
        
        List<Object[]> results = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            
            while (rs.next()) {
                results.add(new Object[]{
                    rs.getString("location"),
                    rs.getInt("hotel_count"),
                    rs.getInt("total_suites")
                });
            }
        }
        return results;
    }

    /**
     * Check if hotel exists
     */
    public boolean hotelExists(int hotelId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hotel WHERE hotel_id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
            return false;
        }
    }

    /**
     * Get hotels with minimum total suites
     * Covers: Comparison condition
     */
    public List<Hotel> getHotelsWithMinSuites(int minSuites) throws SQLException {
        String sql = "SELECT * FROM hotel WHERE total_suites >= ? ORDER BY total_suites DESC";
        List<Hotel> hotels = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, minSuites);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                Hotel hotel = new Hotel(
                    rs.getString("name"),
                    rs.getString("theme"),
                    rs.getString("location"),
                    rs.getInt("total_suites")
                );
                // hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    public List<GuestExperience> getExperiencesByHotel(int hotelId) throws SQLException {
        String sql = """
            SELECT ge.experience_id, ge.hotel_id, ge.experience_name, ge.description, ge.base_cost
            FROM guest_experience ge
            WHERE ge.hotel_id = ?
            ORDER BY ge.experience_name
        """;
        
        List<GuestExperience> experiences = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                GuestExperience experience = new GuestExperience(
                    rs.getInt("hotel_id"),
                    rs.getString("experience_name"),
                    rs.getString("description"),
                    rs.getDouble("base_cost")
                );
                experience.setExperienceId(rs.getInt("experience_id"));
                experiences.add(experience);
            }
        }
        return experiences;
    }
}
