package dao;

import model.Product;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * SQL queries for the products table. Every query uses PreparedStatement.
 */
public class ProductDao {

    /** Adds a new product. Returns false if it could not be saved. */
    public boolean addProduct(String name, double wholesalePrice, double retailPrice, int stockQty) {
        String sql = "INSERT INTO products (product_name, wholesale_price, retail_price, stock_qty) "
                + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setDouble(2, wholesalePrice);
            ps.setDouble(3, retailPrice);
            ps.setInt(4, stockQty);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.out.println("Could not add product: " + e.getMessage());
            return false;
        }
    }

    public List<Product> getAllProducts() {
        String sql = "SELECT product_id, product_name, wholesale_price, retail_price, stock_qty "
                + "FROM products ORDER BY product_id";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                products.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return products;
    }

    /** Finds products by exact ID or by part of the name. */
    public List<Product> searchProducts(String keyword) {
        String sql = "SELECT product_id, product_name, wholesale_price, retail_price, stock_qty "
                + "FROM products WHERE CAST(product_id AS TEXT) = ? OR product_name LIKE ? "
                + "ORDER BY product_id";
        List<Product> products = new ArrayList<>();
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, keyword);
            ps.setString(2, "%" + keyword + "%");
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(mapRow(rs));
                }
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
        }
        return products;
    }

    public Optional<Product> findById(int productId) {
        String sql = "SELECT product_id, product_name, wholesale_price, retail_price, stock_qty "
                + "FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
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

    /** True if any consignment still refers to this product. */
    public boolean isProductInUse(int productId) {
        String sql = "SELECT 1 FROM consignments WHERE product_id = ? LIMIT 1";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (SQLException e) {
            System.out.println("Database error: " + e.getMessage());
            return true;
        }
    }

    /** Removes a discontinued product. Returns true if a row was deleted. */
    public boolean deleteProduct(int productId) {
        String sql = "DELETE FROM products WHERE product_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, productId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("Could not delete product: " + e.getMessage());
            return false;
        }
    }

    private Product mapRow(ResultSet rs) throws SQLException {
        return new Product(
                rs.getInt("product_id"),
                rs.getString("product_name"),
                rs.getDouble("wholesale_price"),
                rs.getDouble("retail_price"),
                rs.getInt("stock_qty"));
    }
}
