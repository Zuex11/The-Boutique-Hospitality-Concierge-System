package dao;

import db.DatabaseConnection;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class HotelDAO {
    private final DatabaseConnection db;

    public HotelDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }


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

                    hotel.setHotelId(generatedKeys.getInt(1));
                }
            }
        }
    }

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


    public void deleteHotel(int hotelId) throws SQLException {
        String sql = "DELETE FROM hotel WHERE hotel_id = ?";
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            stmt.executeUpdate();
        }
    }


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

                 hotel.setHotelId(rs.getInt("hotel_id"));
                return hotel;
            }
            return null;
        }
    }


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
                 hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }


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
                 hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }


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
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    public List<SuiteClass> getHotelSuiteClasses(int hotelId) throws SQLException {
        String sql = """
    SELECT DISTINCT sc.class_id, sc.class_name, sc.nightly_rate, sc.amenities
    FROM suite_class sc
    JOIN suite s ON s.class_id = sc.class_id
    WHERE s.hotel_id = ?
    ORDER BY sc.nightly_rate
""";
        
        List<SuiteClass> suiteClasses = new ArrayList<>();
        
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                SuiteClass suiteClass = new SuiteClass(
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
                hotel.setHotelId(rs.getInt("hotel_id"));
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
    public void insertSuiteClass(SuiteClass sc) throws SQLException {
        String sql = "INSERT INTO suite_class (class_name, nightly_rate, amenities) VALUES (?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setString(1, sc.getClassName());
        stmt.setDouble(2, sc.getNightlyRate());
        stmt.setString(3, sc.getAmenities());
        stmt.executeUpdate();
    }

    public void insertSuite(Suite suite) throws SQLException {
        String sql = "INSERT INTO suite (hotel_id, class_id, suite_number) VALUES (?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, suite.getHotelId());
        stmt.setInt(2, suite.getClassId());
        stmt.setString(3, suite.getSuiteNumber());
        stmt.executeUpdate();
    }

    public List<SuiteClass> getAllSuiteClasses() throws SQLException {
        String sql = "SELECT * FROM suite_class ORDER BY class_id";
        List<SuiteClass> list = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            SuiteClass sc = new SuiteClass(

                    rs.getString("class_name"),
                    rs.getDouble("nightly_rate"),
                    rs.getString("amenities")
            );
            sc.setClassId(rs.getInt("class_id"));
            list.add(sc);
        }
        return list;
    }
}
