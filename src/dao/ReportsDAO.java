package dao;

import db.DatabaseConnection;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ReportsDAO {
    private final DatabaseConnection db;

    public ReportsDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    // 1. Most popular suite class by reservation count
    public String getMostPopularSuiteClass() throws SQLException {
        String sql = """
            SELECT TOP 1 sc.class_name, COUNT(r.reservation_id) AS total
            FROM reservation r
            JOIN suite s ON r.suite_id = s.suite_id
            JOIN suite_class sc ON s.class_id = sc.class_id
            GROUP BY sc.class_name
            ORDER BY total DESC
        """;
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getString("class_name") + " — " + rs.getInt("total") + " reservations";
        return "No data.";
    }

    // 2. Hotels with zero experiences last month
    public List<String> getHotelsWithNoExperiencesLastMonth() throws SQLException {
        String sql = """
            SELECT h.name
            FROM hotel h
            WHERE h.hotel_id NOT IN (
                SELECT s.hotel_id
                FROM reservation_experience re
                JOIN reservation r ON re.reservation_id = r.reservation_id
                JOIN suite s ON r.suite_id = s.suite_id
                WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("name"));
        if (results.isEmpty()) results.add("All hotels had experiences last month.");
        return results;
    }

    // 3. Concierge with highest total experience value last month
    public String getTopConciergeLastMonth() throws SQLException {
        String sql = """
            SELECT TOP 1 c.full_name, SUM(re.actual_cost) AS total_value
            FROM reservation_experience re
            JOIN concierge c ON re.concierge_id = c.concierge_id
            WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
              AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            GROUP BY c.full_name
            ORDER BY total_value DESC
        """;
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        if (rs.next())
            return rs.getString("full_name") + " — $" + String.format("%.2f", rs.getDouble("total_value"));
        return "No data.";
    }

    // 4. Guests who reserved but booked no experiences last month
    public List<String> getGuestsWithNoExperiencesLastMonth() throws SQLException {
        String sql = """
            SELECT g.full_name, g.email
            FROM guest g
            WHERE g.guest_id IN (
                SELECT r.guest_id
                FROM reservation r
                WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(r.check_in) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
            AND g.guest_id NOT IN (
                SELECT r.guest_id
                FROM reservation_experience re
                JOIN reservation r ON re.reservation_id = r.reservation_id
                WHERE MONTH(re.booked_date) = MONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND YEAR(re.booked_date) = YEAR(DATEADD(MONTH, -1, GETDATE()))
            )
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("full_name") + " | " + rs.getString("email"));
        if (results.isEmpty()) results.add("No guests without experiences last month.");
        return results;
    }

    // 5. Available suites per hotel last month
    public List<String> getAvailableSuitesPerHotelLastMonth() throws SQLException {
        String sql = """
            SELECT h.name AS hotel_name, s.suite_number
            FROM suite s
            JOIN hotel h ON s.hotel_id = h.hotel_id
            WHERE s.suite_id NOT IN (
                SELECT r.suite_id
                FROM reservation r
                WHERE r.check_in <= EOMONTH(DATEADD(MONTH, -1, GETDATE()))
                  AND r.check_out >= DATEFROMPARTS(
                        YEAR(DATEADD(MONTH, -1, GETDATE())),
                        MONTH(DATEADD(MONTH, -1, GETDATE())),
                        1)
            )
            ORDER BY h.name, s.suite_number
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("hotel_name") + " — Suite " + rs.getString("suite_number"));
        if (results.isEmpty()) results.add("No available suites last month.");
        return results;
    }

    // 6. Each guest's profile + total suite spend last month
    public List<String> getGuestSpendLastMonth() throws SQLException {
        String sql = """
            SELECT g.full_name, g.email, g.loyalty_tier, SUM(r.total_cost) AS spent
            FROM guest g
            JOIN reservation r ON g.guest_id = r.guest_id
            WHERE MONTH(r.check_in) = MONTH(DATEADD(MONTH, -1, GETDATE()))
              AND YEAR(r.check_in) = YEAR(DATEADD(MONTH, -1, GETDATE()))
              AND r.status = 'checked-out'
            GROUP BY g.full_name, g.email, g.loyalty_tier
            ORDER BY spent DESC
        """;
        List<String> results = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        while (rs.next())
            results.add(rs.getString("full_name") + " | " +
                        rs.getString("email") + " | " +
                        rs.getString("loyalty_tier") + " | $" +
                        String.format("%.2f", rs.getDouble("spent")));
        if (results.isEmpty()) results.add("No checkout data last month.");
        return results;
    }
}