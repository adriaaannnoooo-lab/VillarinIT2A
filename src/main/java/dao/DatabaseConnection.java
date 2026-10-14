package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Handles the SQLite connection and creates the tables on first run.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:konsigna.db";

    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }

    /** Creates the users table if it does not exist yet. */
    public static void initialize() {
        String sql = """
                CREATE TABLE IF NOT EXISTS users (
                    user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                    username  TEXT NOT NULL UNIQUE,
                    password  TEXT NOT NULL,
                    full_name TEXT NOT NULL,
                    role      TEXT NOT NULL CHECK (role IN ('DISTRIBUTOR', 'STAFF', 'RESELLER')),
                    status    TEXT NOT NULL DEFAULT 'PENDING'
                )
                """;
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
        }
    }
}