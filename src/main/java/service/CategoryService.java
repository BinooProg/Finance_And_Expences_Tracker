package service;

import model.Category;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CategoryService {

    private final Connection con;

    public CategoryService() {
        this.con = DatabaseConnection.getDatabaseConnection().getConnection();
    }

    public List<Category> getAllCategories() {
        List<Category> categories = new ArrayList<>();

        String sql = "SELECT id, name FROM Categories ORDER BY id";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("id"),
                        rs.getString("name")
                );

                categories.add(category);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories from database.", e);
        }

        return categories;
    }

    public void addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        if (categoryExists(name)) {
            throw new IllegalArgumentException("Category already exists.");
        }

        String sql = "INSERT INTO Categories (name) VALUES (?)";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name.trim());

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category in database.", e);
        }
    }

    public void updateCategory(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        if (categoryExistsForAnotherId(id, newName)) {
            throw new IllegalArgumentException("Category already exists.");
        }

        String sql = "UPDATE Categories SET name = ? WHERE id = ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, newName.trim());
            ps.setInt(2, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Category not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category in database.", e);
        }
    }

    public void deleteCategory(int id) {
        String sql = "DELETE FROM Categories WHERE id = ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Category not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException(
                    "Cannot delete category. It may be used by existing transactions.", e );
        }
    }

    private boolean categoryExists(String name) {
        String sql = "SELECT id FROM Categories WHERE LOWER(name) = LOWER(?)";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name.trim());

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check category existence.", e);
        }
    }

    private boolean categoryExistsForAnotherId(int id, String name) {
        String sql = "SELECT id FROM Categories WHERE LOWER(name) = LOWER(?) AND id <> ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, name.trim());
            ps.setInt(2, id);

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check category existence.", e);
        }
    }
}