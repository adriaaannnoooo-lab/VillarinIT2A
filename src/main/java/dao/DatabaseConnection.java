package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Opens SQLite connections. Foreign key enforcement is switched on
 * every time a connection is created.
 */
public class DatabaseConnection {

    private static final String URL = "jdbc:sqlite:konsigna.db";

    private DatabaseConnection() { }

    public static Connection getConnection() throws SQLException {
        Connection conn = DriverManager.getConnection(URL);
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON;");
        }
        return conn;
    }
}