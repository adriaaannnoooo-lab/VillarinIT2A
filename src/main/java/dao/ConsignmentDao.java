package dao;

import model.ConsignmentView;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL queries for consignments and their remittances.
 * Multi-step changes (issuing stock, returns, payments) run as one transaction,
 * so stock and balances never end up half-updated.
 */
public class ConsignmentDao {

    /** One query for all views: pass 0 to skip a filter. */
    private static final String VIEW_QUERY = """
            SELECT c.consignment_id, c.reseller_id, r.full_name AS reseller_name, p.product_name,
                   c.qty_issued, c.qty_sold, c.qty_returned, c.date_issued, c.due_date, c.status,
                   p.wholesale_price, p.retail_price,
                   COALESCE((SELECT SUM(m.amount) FROM remittances m
                             WHERE m.consignment_id = c.consignment_id), 0) AS remitted
            FROM consignments c
            JOIN resellers r ON r.reseller_id = c.reseller_id
            JOIN products p ON p.product_id = c.product_id
            WHERE (? = 0 OR c.reseller_id = ?) AND (? = 0 OR c.consignment_id = ?)
            ORDER BY c.consignment_id
            """;

    // ---------- Read ----------

    public List<ConsignmentView> getAll() {
        return query(0, 0);
    }

    public List<ConsignmentView> getByReseller(int resellerId) {
        return query(resellerId, 0);
    }

    public Optional<ConsignmentView> getById(int consignmentId) {
        List<ConsignmentView> list = query(0, consignmentId);
        return list.isEmpty() ? Optional.empty() : Optional.of(list.get(0));
    }

    // ---------- Create: issue stock ----------

    /** Takes stock out of the product and creates the consignment. Returns false if stock is short. */
    public boolean issueStock(int resellerId, int productId, int qty, int dueDays) {
        String deduct = "UPDATE products SET stock_qty = stock_qty - ? WHERE product_id = ? AND stock_qty >= ?";
        String insert = "INSERT INTO consignments (reseller_id, product_id, qty_issued, qty_sold, "
                + "qty_returned, date_issued, due_date, status) VALUES (?, ?, ?, 0, 0, ?, ?, 'Issued')";
        LocalDate today = LocalDate.now();
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement d = conn.prepareStatement(deduct);
                 PreparedStatement i = conn.prepareStatement(insert)) {
                d.setInt(1, qty);
                d.setInt(2, productId);
                d.setInt(3, qty);
                if (d.executeUpdate() == 0) {
                    conn.rollback();
                    return false;
                }
                i.setInt(1, resellerId);
                i.setInt(2, productId);
                i.setInt(3, qty);
                i.setString(4, today.toString());
                i.setString(5, today.plusDays(dueDays).toString());
                i.executeUpdate();
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not issue stock: " + e.getMessage());
            return false;
        }
    }

    // ---------- Update: sold and returned ----------

    /** Adds to the sold and returned counts. Returned items go back to product stock. */
    public boolean recordSoldReturned(int consignmentId, int addSold, int addReturned) {
        String select = "SELECT product_id, qty_issued, qty_sold, qty_returned "
                + "FROM consignments WHERE consignment_id = ?";
        String update = "UPDATE consignments SET qty_sold = qty_sold + ?, "
                + "qty_returned = qty_returned + ? WHERE consignment_id = ?";
        String restock = "UPDATE products SET stock_qty = stock_qty + ? WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement sel = conn.prepareStatement(select);
                 PreparedStatement upd = conn.prepareStatement(update);
                 PreparedStatement stk = conn.prepareStatement(restock)) {
                int productId = 0;
                int remaining = 0;
                sel.setInt(1, consignmentId);
                try (ResultSet rs = sel.executeQuery()) {
                    if (!rs.next()) {
                        conn.rollback();
                        return false;
                    }
                    productId = rs.getInt("product_id");
                    remaining = rs.getInt("qty_issued") - rs.getInt("qty_sold") - rs.getInt("qty_returned");
                }
                if (addSold < 0 || addReturned < 0 || addSold + addReturned > remaining) {
                    conn.rollback();
                    return false;
                }
                upd.setInt(1, addSold);
                upd.setInt(2, addReturned);
                upd.setInt(3, consignmentId);
                upd.executeUpdate();
                stk.setInt(1, addReturned);
                stk.setInt(2, productId);
                stk.executeUpdate();
                recomputeStatus(conn, consignmentId);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not update quantities: " + e.getMessage());
            return false;
        }
    }

    // ---------- Update: remittance ----------

    /** Records a payment and updates the consignment status. Amount cannot exceed the balance. */
    public boolean recordRemittance(int consignmentId, int staffUserId, double amount) {
        String insert = "INSERT INTO remittances (consignment_id, user_id, amount, remit_date) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection()) {
            conn.setAutoCommit(false);
            try (PreparedStatement ins = conn.prepareStatement(insert)) {
                double[] dueAndPaid = dueAndRemitted(conn, consignmentId);
                if (dueAndPaid == null || amount <= 0
                        || amount > (dueAndPaid[0] - dueAndPaid[1]) + 0.005) {
                    conn.rollback();
                    return false;
                }
                ins.setInt(1, consignmentId);
                ins.setInt(2, staffUserId);
                ins.setDouble(3, amount);
                ins.setString(4, LocalDate.now().toString());
                ins.executeUpdate();
                recomputeStatus(conn, consignmentId);
                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            System.out.println("Could not record remittance: " + e.getMessage());
            return false;
        }
    }

    /** Flags unsettled consignments whose due date has passed as Overdue. */
    public void refreshOverdueStatuses() {
        String sql = "UPDATE consignments SET status = 'Overdue' "
                + "WHERE due_date < ? AND status IN ('Issued', 'Partially Paid')";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, LocalDate.now().toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
    }

    // ---------- Helpers ----------

    private List<ConsignmentView> query(int resellerId, int consignmentId) {
        List<ConsignmentView> list = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(VIEW_QUERY)) {
            ps.setInt(1, resellerId);
            ps.setInt(2, resellerId);
            ps.setInt(3, consignmentId);
            ps.setInt(4, consignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return list;
    }

    /** Returns {amount due, total remitted} for a consignment, or null if it does not exist. */
    private double[] dueAndRemitted(Connection conn, int consignmentId) throws SQLException {
        String sql = """
                SELECT c.qty_sold * p.wholesale_price AS due,
                       COALESCE((SELECT SUM(m.amount) FROM remittances m
                                 WHERE m.consignment_id = c.consignment_id), 0) AS remitted
                FROM consignments c
                JOIN products p ON p.product_id = c.product_id
                WHERE c.consignment_id = ?
                """;
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, consignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return new double[] { rs.getDouble("due"), rs.getDouble("remitted") };
                }
            }
        }
        return null;
    }

    /**
     * Settled   = every item sold or returned and fully paid
     * Overdue   = not settled and past the due date
     * Partially Paid = some money received
     * Issued    = nothing paid yet
     */
    private void recomputeStatus(Connection conn, int consignmentId) throws SQLException {
        String select = """
                SELECT c.qty_issued, c.qty_sold, c.qty_returned, c.due_date, p.wholesale_price,
                       COALESCE((SELECT SUM(m.amount) FROM remittances m
                                 WHERE m.consignment_id = c.consignment_id), 0) AS remitted
                FROM consignments c
                JOIN products p ON p.product_id = c.product_id
                WHERE c.consignment_id = ?
                """;
        String update = "UPDATE consignments SET status = ? WHERE consignment_id = ?";
        String status;
        try (PreparedStatement ps = conn.prepareStatement(select)) {
            ps.setInt(1, consignmentId);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return;
                }
                int issued = rs.getInt("qty_issued");
                int sold = rs.getInt("qty_sold");
                int returned = rs.getInt("qty_returned");
                double due = sold * rs.getDouble("wholesale_price");
                double remitted = rs.getDouble("remitted");
                boolean allAccountedFor = (sold + returned) == issued;

                if (allAccountedFor && remitted >= due - 0.005) {
                    status = ConsignmentView.SETTLED;
                } else if (LocalDate.now().toString().compareTo(rs.getString("due_date")) > 0) {
                    status = ConsignmentView.OVERDUE;
                } else if (remitted > 0) {
                    status = ConsignmentView.PARTIALLY_PAID;
                } else {
                    status = ConsignmentView.ISSUED;
                }
            }
        }
        try (PreparedStatement ps = conn.prepareStatement(update)) {
            ps.setString(1, status);
            ps.setInt(2, consignmentId);
            ps.executeUpdate();
        }
    }

    private ConsignmentView mapRow(ResultSet rs) throws SQLException {
        return new ConsignmentView(
                rs.getInt("consignment_id"),
                rs.getInt("reseller_id"),
                rs.getString("reseller_name"),
                rs.getString("product_name"),
                rs.getInt("qty_issued"),
                rs.getInt("qty_sold"),
                rs.getInt("qty_returned"),
                rs.getString("date_issued"),
                rs.getString("due_date"),
                rs.getString("status"),
                rs.getDouble("wholesale_price"),
                rs.getDouble("retail_price"),
                rs.getDouble("remitted"));
    }
}
