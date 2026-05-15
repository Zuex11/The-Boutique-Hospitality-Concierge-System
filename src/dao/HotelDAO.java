package dao;

import db.DatabaseConnection;
import models.*;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Data Access Object (DAO) for hotel, suite class, suite, and guest experience
 * operations in the MIRAGE system.
 *
 * <p>This is the broadest DAO in the system, covering the entire hotel-property
 * domain. It is used primarily by {@code HotelSuiteScreen} to register and
 * browse hotels, suite classes, and individual suites. It is also used by
 * other DAOs and screens that need to look up hotel-related metadata.</p>
 *
 * <h2>Responsibilities</h2>
 * <ul>
 *   <li>Full CRUD for {@link Hotel} records</li>
 *   <li>Insert and query {@link SuiteClass} records</li>
 *   <li>Insert {@link Suite} records</li>
 *   <li>Query {@link GuestExperience} records by hotel</li>
 *   <li>Aggregate queries (total suites, hotel count by location)</li>
 * </ul>
 */
public class HotelDAO {

    /** Shared database connection obtained from the singleton. */
    private final DatabaseConnection db;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Constructs a new {@code HotelDAO} and acquires the shared database connection.
     *
     * @throws SQLException if the database connection cannot be established
     */
    public HotelDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    // ── Hotel CRUD ───────────────────────────────────────────────────────────

    /**
     * Inserts a new hotel record into the {@code hotel} table and writes the
     * generated primary key back to the supplied {@link Hotel} object.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   INSERT INTO hotel (name, theme, location, total_suites) VALUES (?, ?, ?, ?)
     * }</pre>
     *
     * @param hotel the {@link Hotel} to persist; must have a non-null {@code name}
     * @throws SQLException if a database error occurs during insertion
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
                    hotel.setHotelId(generatedKeys.getInt(1));
                }
            }
        }
    }

    /**
     * Updates all mutable fields of an existing hotel record.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   UPDATE hotel SET name=?, theme=?, location=?, total_suites=?
     *   WHERE hotel_id=?
     * }</pre>
     *
     * @param hotel the {@link Hotel} with updated values; {@code hotelId} must
     *              match an existing record
     * @throws SQLException if the hotel ID does not exist or a database error occurs
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
     * Deletes a hotel record by its primary key.
     *
     * <p><b>Warning:</b> This will fail with a FK violation if any suites,
     * concierge staff, or guest experiences still reference this hotel.
     * Those child records must be removed first.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   DELETE FROM hotel WHERE hotel_id = ?
     * }</pre>
     *
     * @param hotelId the ID of the hotel to delete; must be positive
     * @throws SQLException if child records exist (FK violation) or a database error occurs
     */
    public void deleteHotel(int hotelId) throws SQLException {
        String sql = "DELETE FROM hotel WHERE hotel_id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            stmt.executeUpdate();
        }
    }

    /**
     * Retrieves a single hotel by its primary key.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM hotel WHERE hotel_id = ?
     * }</pre>
     *
     * @param hotelId the ID of the hotel to look up; must be positive
     * @return the matching {@link Hotel}, or {@code null} if not found
     * @throws SQLException if a database error occurs during the query
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
                        rs.getInt("total_suites"));
                hotel.setHotelId(rs.getInt("hotel_id"));
                return hotel;
            }
            return null;
        }
    }

    /**
     * Retrieves all hotel records, ordered by {@code hotel_id} ascending.
     *
     * <p>Used to populate the hotel table in {@code HotelSuiteScreen} and
     * the hotel combo boxes in other screens.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM hotel ORDER BY hotel_id
     * }</pre>
     *
     * @return a {@link List} of all {@link Hotel} objects; empty if none exist
     * @throws SQLException if a database error occurs during the query
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
                        rs.getInt("total_suites"));
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    /**
     * Retrieves hotels whose location contains the given substring (case-insensitive).
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM hotel WHERE location LIKE ? ORDER BY name
     * }</pre>
     *
     * @param location the location substring to search for; must not be {@code null}
     * @return a {@link List} of matching {@link Hotel} objects; empty if none match
     * @throws SQLException if a database error occurs during the query
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
                        rs.getInt("total_suites"));
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    /**
     * Retrieves hotels with the exact given theme.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM hotel WHERE theme = ? ORDER BY name
     * }</pre>
     *
     * @param theme the exact theme to filter by; must not be {@code null}
     * @return a {@link List} of matching {@link Hotel} objects; empty if none match
     * @throws SQLException if a database error occurs during the query
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
                        rs.getInt("total_suites"));
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    // ── Suite Class ──────────────────────────────────────────────────────────

    /**
     * Inserts a new suite class record into the {@code suite_class} table.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   INSERT INTO suite_class (class_name, nightly_rate, amenities) VALUES (?, ?, ?)
     * }</pre>
     *
     * @param sc the {@link SuiteClass} to persist; {@code className} must not be blank
     * @throws SQLException if a database error occurs during insertion
     */
    public void insertSuiteClass(SuiteClass sc) throws SQLException {
        String sql = "INSERT INTO suite_class (class_name, nightly_rate, amenities) VALUES (?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setString(1, sc.getClassName());
        stmt.setDouble(2, sc.getNightlyRate());
        stmt.setString(3, sc.getAmenities());
        stmt.executeUpdate();
    }

    /**
     * Retrieves all suite class records, ordered by {@code class_id} ascending.
     *
     * <p>Used to populate the suite class table and the class combo box in
     * the "Add Suite" form.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM suite_class ORDER BY class_id
     * }</pre>
     *
     * @return a {@link List} of all {@link SuiteClass} objects; empty if none exist
     * @throws SQLException if a database error occurs during the query
     */
    public List<SuiteClass> getAllSuiteClasses() throws SQLException {
        String sql = "SELECT * FROM suite_class ORDER BY class_id";
        List<SuiteClass> list = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            SuiteClass sc = new SuiteClass(
                    rs.getString("class_name"),
                    rs.getDouble("nightly_rate"),
                    rs.getString("amenities"));
            sc.setClassId(rs.getInt("class_id"));
            list.add(sc);
        }
        return list;
    }

    /**
     * Retrieves the distinct suite classes used by suites within a specific hotel.
     *
     * <p>Results are ordered by nightly rate ascending so the cheapest tier
     * appears first.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT DISTINCT sc.class_id, sc.class_name, sc.nightly_rate, sc.amenities
     *   FROM suite_class sc
     *   JOIN suite s ON s.class_id = sc.class_id
     *   WHERE s.hotel_id = ?
     *   ORDER BY sc.nightly_rate
     * }</pre>
     *
     * @param hotelId the ID of the hotel to filter by; must be positive
     * @return a {@link List} of {@link SuiteClass} objects used in that hotel;
     *         empty if the hotel has no suites or suite classes
     * @throws SQLException if a database error occurs during the query
     */
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
                        rs.getString("amenities"));
                suiteClass.setClassId(rs.getInt("class_id"));
                suiteClasses.add(suiteClass);
            }
        }
        return suiteClasses;
    }

    // ── Suite ────────────────────────────────────────────────────────────────

    /**
     * Inserts a new suite record into the {@code suite} table.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   INSERT INTO suite (hotel_id, class_id, suite_number) VALUES (?, ?, ?)
     * }</pre>
     *
     * @param suite the {@link Suite} to persist; must have valid {@code hotelId},
     *              {@code classId}, and a non-blank {@code suiteNumber}
     * @throws SQLException if the hotel or class ID does not exist (FK violation)
     *                      or a database error occurs
     */
    public void insertSuite(Suite suite) throws SQLException {
        String sql = "INSERT INTO suite (hotel_id, class_id, suite_number) VALUES (?, ?, ?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, suite.getHotelId());
        stmt.setInt(2, suite.getClassId());
        stmt.setString(3, suite.getSuiteNumber());
        stmt.executeUpdate();
    }

    // ── Aggregates ───────────────────────────────────────────────────────────

    /**
     * Returns the sum of {@code total_suites} across all hotel records.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT SUM(total_suites) AS total_suites FROM hotel
     * }</pre>
     *
     * @return the total suite count across all hotels; {@code 0} if no hotels exist
     * @throws SQLException if a database error occurs during the query
     */
    public int getTotalSuitesAcrossAllHotels() throws SQLException {
        String sql = "SELECT SUM(total_suites) as total_suites FROM hotel";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            if (rs.next()) return rs.getInt("total_suites");
            return 0;
        }
    }

    /**
     * Returns a breakdown of hotel count and total suites grouped by location.
     *
     * <p>Each element in the returned list is an {@code Object[]} with three entries:
     * {@code [location (String), hotel_count (int), total_suites (int)]}.</p>
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT location, COUNT(*) AS hotel_count, SUM(total_suites) AS total_suites
     *   FROM hotel
     *   GROUP BY location
     *   ORDER BY location
     * }</pre>
     *
     * @return a {@link List} of {@code Object[]} rows; empty if no hotels exist
     * @throws SQLException if a database error occurs during the query
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
     * Checks whether a hotel with the given ID exists in the database.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT COUNT(*) FROM hotel WHERE hotel_id = ?
     * }</pre>
     *
     * @param hotelId the ID to check; must be positive
     * @return {@code true} if a record exists; {@code false} otherwise
     * @throws SQLException if a database error occurs during the query
     */
    public boolean hotelExists(int hotelId) throws SQLException {
        String sql = "SELECT COUNT(*) FROM hotel WHERE hotel_id = ?";
        try (PreparedStatement stmt = db.getConnection().prepareStatement(sql)) {
            stmt.setInt(1, hotelId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
            return false;
        }
    }

    /**
     * Retrieves hotels that have at least the specified number of suites,
     * ordered by suite count descending (largest first).
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT * FROM hotel WHERE total_suites >= ? ORDER BY total_suites DESC
     * }</pre>
     *
     * @param minSuites the minimum number of suites required; must be non-negative
     * @return a {@link List} of matching {@link Hotel} objects; empty if none qualify
     * @throws SQLException if a database error occurs during the query
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
                        rs.getInt("total_suites"));
                hotel.setHotelId(rs.getInt("hotel_id"));
                hotels.add(hotel);
            }
        }
        return hotels;
    }

    // ── Guest Experience ─────────────────────────────────────────────────────

    /**
     * Retrieves all guest experiences offered by a specific hotel,
     * ordered alphabetically by experience name.
     *
     * <p><b>SQL executed:</b></p>
     * <pre>{@code
     *   SELECT ge.experience_id, ge.hotel_id, ge.experience_name,
     *          ge.description, ge.base_cost
     *   FROM guest_experience ge
     *   WHERE ge.hotel_id = ?
     *   ORDER BY ge.experience_name
     * }</pre>
     *
     * @param hotelId the ID of the hotel to query experiences for; must be positive
     * @return a {@link List} of {@link GuestExperience} objects at that hotel;
     *         empty if the hotel has no defined experiences
     * @throws SQLException if a database error occurs during the query
     */
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
                        rs.getDouble("base_cost"));
                experience.setExperienceId(rs.getInt("experience_id"));
                experiences.add(experience);
            }
        }
        return experiences;
    }
}
