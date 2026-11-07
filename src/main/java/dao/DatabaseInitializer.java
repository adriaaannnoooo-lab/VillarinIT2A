package dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Makes sure every table in the Konsigna ERD exists before the menus start.
 */
public class DatabaseInitializer {

    private DatabaseInitializer() { }

    public static void initialize() {
        String[] tables = {
                """
            CREATE TABLE IF NOT EXISTS users (
                user_id   INTEGER PRIMARY KEY AUTOINCREMENT,
                username  TEXT NOT NULL UNIQUE,
                password  TEXT NOT NULL,
                full_name TEXT NOT NULL,
                role      TEXT NOT NULL CHECK (role IN ('DISTRIBUTOR', 'STAFF', 'RESELLER')),
                status    TEXT NOT NULL DEFAULT 'PENDING'
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS products (
                product_id      INTEGER PRIMARY KEY AUTOINCREMENT,
                product_name    TEXT NOT NULL,
                wholesale_price REAL NOT NULL,
                retail_price    REAL NOT NULL,
                stock_qty       INTEGER NOT NULL DEFAULT 0
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS resellers (
                reseller_id INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id     INTEGER NOT NULL,
                full_name   TEXT NOT NULL,
                contact_no  TEXT,
                status      TEXT NOT NULL DEFAULT 'Active' CHECK (status IN ('Active', 'Inactive')),
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS consignments (
                consignment_id INTEGER PRIMARY KEY AUTOINCREMENT,
                reseller_id    INTEGER NOT NULL,
                product_id     INTEGER NOT NULL,
                qty_issued     INTEGER NOT NULL,
                qty_sold       INTEGER NOT NULL DEFAULT 0,
                qty_returned   INTEGER NOT NULL DEFAULT 0,
                date_issued    TIMESTAMP NOT NULL,
                due_date       TIMESTAMP NOT NULL,
                status         TEXT NOT NULL DEFAULT 'Issued'
                               CHECK (status IN ('Issued', 'Partially Paid', 'Settled', 'Overdue')),
                FOREIGN KEY (reseller_id) REFERENCES resellers(reseller_id),
                FOREIGN KEY (product_id)  REFERENCES products(product_id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS remittances (
                remittance_id  INTEGER PRIMARY KEY AUTOINCREMENT,
                consignment_id INTEGER NOT NULL,
                user_id        INTEGER NOT NULL,
                amount         REAL NOT NULL,
                remit_date     TIMESTAMP NOT NULL,
                FOREIGN KEY (consignment_id) REFERENCES consignments(consignment_id),
                FOREIGN KEY (user_id)        REFERENCES users(user_id)
            )
            """,
                """
            CREATE TABLE IF NOT EXISTS activity_logs (
                log_id       INTEGER PRIMARY KEY AUTOINCREMENT,
                user_id      INTEGER NOT NULL,
                action       TEXT NOT NULL,
                description  TEXT,
                log_datetime TIMESTAMP NOT NULL,
                FOREIGN KEY (user_id) REFERENCES users(user_id)
            )
            """
        };

        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {
            for (String sql : tables) {
                stmt.execute(sql);
            }
            System.out.println("Database initialized: all tables are ready.");
        } catch (SQLException e) {
            System.out.println("Database setup failed: " + e.getMessage());
            System.exit(1);
        }
    }
}