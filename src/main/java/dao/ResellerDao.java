package dao;

import model.Reseller;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL queries for the resellers table. Every query uses PreparedStatement.
 */
public class ResellerDao {

    /** Creates a reseller profile for an existing user account. */
    public boolean addReseller(int userId, String fullName, String contactNo) {
        String sql = "INSERT INTO resellers (user_id, full_name, contact_no, status) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userId);
            ps.setString(2, fullName);
            ps.setString(3, contactNo);
            ps.setString(4, Reseller.STATUS_ACTIVE);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Could not add reseller: " + e.getMessage());
            return false;
        }
    }

    public List<Reseller> getAllResellers() {
        String sql = "SELECT reseller_id, user_id, full_name, contact_no, status "
                + "FROM resellers ORDER BY reseller_id";
        List<Reseller> resellers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                resellers.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return resellers;
    }

    /** Finds resellers by exact ID or by part of the name. */
    public List<Reseller> searchResellers(String keyword) {
        String sql = "SELECT reseller_id, user_id, full_name, contact_no, status "
                + "FROM resellers WHERE CAST(reseller_id AS TEXT) = ? OR full_name LIKE ? "
                + "ORDER BY reseller_id";
        List<Reseller> resellers = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    resellers.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return resellers;
    }

    public Optional<Reseller> findById(int resellerId) {
        String sql = "SELECT reseller_id, user_id, full_name, contact_no, status "
                + "FROM resellers WHERE reseller_id = ?";
        return findOne(sql, resellerId);
    }

    /** Finds the reseller profile that belongs to a login account. */
    public Optional<Reseller> findByUserId(int userId) {
        String sql = "SELECT reseller_id, user_id, full_name, contact_no, status "
                + "FROM resellers WHERE user_id = ?";
        return findOne(sql, userId);
    }

    /** Soft delete: marks the reseller Inactive instead of removing the record. */
    public boolean deactivateReseller(int resellerId) {
        String sql = "UPDATE resellers SET status = ? WHERE reseller_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, Reseller.STATUS_INACTIVE);
            ps.setInt(2, resellerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Could not deactivate reseller: " + e.getMessage());
            return false;
        }
    }

    private Optional<Reseller> findOne(String sql, int id) {
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return Optional.empty();
    }

    private Reseller mapRow(ResultSet rs) throws SQLException {
        return new Reseller(
                rs.getInt("reseller_id"),
                rs.getInt("user_id"),
                rs.getString("full_name"),
                rs.getString("contact_no"),
                rs.getString("status"));
    }
}
