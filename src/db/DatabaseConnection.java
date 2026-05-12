package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;databaseName=Database_project"
                    + ";integratedSecurity=true;encrypt=true;trustServerCertificate=true";

    private static DatabaseConnection instance ;
    private Connection connection;

    private DatabaseConnection () throws SQLException {
        this.connection = DriverManager.getConnection(URL); //takes url and opens a connection with the sql server
    }

    public static DatabaseConnection getInstance () throws SQLException {
        if (instance == null || instance.connection.isClosed()) { //avoids opening two connections
            instance = new DatabaseConnection();
        }
        return instance;
    }

    public Connection getConnection(){
        return connection;
    }


      

}