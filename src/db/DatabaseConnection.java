package db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Singleton wrapper around the JDBC {@link Connection} to the MIRAGE SQL Server database.
 *
 * <p>This class implements the <b>Singleton</b> pattern to ensure that only one
 * database connection is active at any given time across the entire application.
 * All DAO classes obtain their connection exclusively through this class via
 * {@link #getInstance()} and {@link #getConnection()}.</p>
 *
 * <h2>Connection details</h2>
 * <ul>
 *   <li>Driver: Microsoft SQL Server (JDBC)</li>
 *   <li>Host: {@code localhost:50983}</li>
 *   <li>Database: {@code Database_project}</li>
 *   <li>Authentication: SQL Server login ({@code sa} / {@code 1234})</li>
 *   <li>Encryption: enabled with server certificate trust</li>
 * </ul>
 *
 * <h2>Reconnection behaviour</h2>
 * <p>Both {@link #getInstance()} and {@link #getConnection()} check whether the
 * current connection is {@code null} or has been closed, and transparently
 * re-establish it if needed. This provides basic resilience against dropped
 * connections without requiring manual intervention.</p>
 *
 * <p><b>Thread safety:</b> This implementation is <em>not</em> thread-safe.
 * It is intended for a single-threaded JavaFX desktop application where all
 * database access occurs on the application thread.</p>
 */
public class DatabaseConnection {

    /**
     * JDBC connection URL for the SQL Server instance.
     * Includes host, port, database name, credentials, and SSL settings.
     */
    private static final String URL =
            "jdbc:sqlserver://localhost:50983;databaseName=Database_project"
                    + ";integratedSecurity=false;encrypt=true;trustServerCertificate=true"
                    + ";user=sa;password=1234";

    /** The single shared instance of this class (Singleton). */
    private static DatabaseConnection instance;

    /** The underlying JDBC connection managed by this singleton. */
    private Connection connection;

    // ── Constructor ──────────────────────────────────────────────────────────

    /**
     * Private constructor — prevents direct instantiation.
     * Opens a new JDBC connection using the configured {@link #URL}.
     *
     * @throws SQLException if the driver cannot establish a connection
     *                      (e.g. wrong credentials, server unreachable)
     */
    private DatabaseConnection() throws SQLException {
        this.connection = DriverManager.getConnection(URL);
    }

    // ── Singleton accessor ───────────────────────────────────────────────────

    /**
     * Returns the shared {@code DatabaseConnection} instance, creating it if
     * it does not yet exist or if the underlying connection has been closed.
     *
     * <p>This is the entry point used by all DAO constructors:</p>
     * <pre>{@code
     *   this.db = DatabaseConnection.getInstance();
     * }</pre>
     *
     * @return the singleton {@code DatabaseConnection} instance
     * @throws SQLException if a new connection cannot be established
     */
    public static DatabaseConnection getInstance() throws SQLException {
        if (instance == null || instance.connection.isClosed()) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // ── Connection accessor ──────────────────────────────────────────────────

    /**
     * Returns the active {@link Connection}, re-establishing it first if it
     * has been closed since the last call.
     *
     * <p>DAOs call this method each time they need to prepare a statement:</p>
     * <pre>{@code
     *   PreparedStatement stmt = db.getConnection().prepareStatement(sql);
     * }</pre>
     *
     * @return a live, open JDBC {@link Connection}
     * @throws SQLException if the connection is closed and cannot be re-opened
     */
    public Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            connection = DriverManager.getConnection(URL);
        }
        return connection;
    }
}
