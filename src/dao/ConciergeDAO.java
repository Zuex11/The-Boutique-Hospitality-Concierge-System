package dao;
 
import db.DatabaseConnection;
import models.Concierge;
 
import java.sql.*;
import java.util.ArrayList;
import java.util.List;


public class ConciergeDAO {
    private final DatabaseConnection db;

    public ConciergeDAO() throws SQLException {
        this.db = DatabaseConnection.getInstance();
    }

    public void insertConcierge (Concierge concierge) throws SQLException{
        String sql = "INSERT INTO concierge (full_name , hotel_id) VALUES (?,?)";
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        
        stmt.setString(1,concierge.getFullName());
        stmt.setInt(2,concierge.getHotelId());
        stmt.executeUpdate();
    }

    public void deleteConcierge(int conciergeId) throws SQLException {
        String sql = "DELETE FROM Concierge WHERE concierge_id = ?";

        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, conciergeId);
        stmt.executeUpdate();
    }

    public List<Concierge> getAllConcierges() throws SQLException {
        String sql = "SELECT * FROM concierge";

        List<Concierge> concierges = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        ResultSet result_set = stmt.executeQuery();

         while (result_set.next()) {
            Concierge c = new Concierge(
                result_set.getString("full_name"),
                result_set.getInt("hotel_id")
            );
            c.setConciergeId(result_set.getInt("concierge_id"));
            concierges.add(c);
        }
        return concierges;

    }

    public List<Concierge> getConciergesByHotel(int hotelId) throws SQLException {
        String sql = " SELECT * FROM concierge WHERE concierge.hotel_id = ?";

        List<Concierge> list = new ArrayList<>();
        PreparedStatement stmt = db.getConnection().prepareStatement(sql);
        stmt.setInt(1, hotelId);
        ResultSet result_set = stmt.executeQuery();
 
        while (result_set.next()) {
            Concierge c = new Concierge(
                result_set.getString("full_name"),
                result_set.getInt("hotel_id")
            );
            c.setConciergeId(result_set.getInt("concierge_id"));
            list.add(c);
        }
        return list;


    }

    
}
