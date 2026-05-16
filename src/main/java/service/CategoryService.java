package service;

import model.Category;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CategoryService {
    private final List<Category> categories = new ArrayList<>();
    private Integer loadedUserId;
    private final UserService userService = new UserService();

    public List<Category> getAllCategories() {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            categories.clear();
            loadedUserId = null;
            return categories;
        }

        categories.clear();
        loadedUserId = currentUserId;

        String sql = """
                SELECT id, name
                FROM Categories
                WHERE user_id = ?
                ORDER BY id
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    categories.add(new Category(resultSet.getInt("id"), resultSet.getString("name")));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load categories.", e);
        }

        return categories;
    }

    public void addCategory(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        getAllCategories();

        String trimmedName = name.trim();
        String normalizedInput = normalizeName(trimmedName);

        for (Category c : categories) {
            String normalizedExistingName = normalizeName(c.getName());
            if (normalizedExistingName.equals(normalizedInput)) {
                throw new IllegalArgumentException("Category already exists.");
            }
        }

        String sql = "INSERT INTO Categories (user_id, name) VALUES (?, ?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUserId);
            statement.setString(2, trimmedName);
            statement.executeUpdate();
            categories.clear();
            getAllCategories();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save category.", e);
        }
    }

    public void updateCategory(int id, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Category name is required.");
        }

        getAllCategories();

        String trimmedName = newName.trim();
        String normalizedInput = normalizeName(trimmedName);
        boolean found = false;

        for (Category category : categories) {
            String normalizedExistingName = normalizeName(category.getName());
            if (category.getId() != id && normalizedExistingName.equals(normalizedInput)) {
                throw new IllegalArgumentException("Category already exists.");
            }
        }

        for (Category category : categories) {
            if (category.getId() == id) {
                category.setName(trimmedName);
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Category not found.");
        }

        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        String sql = "UPDATE Categories SET name = ? WHERE id = ? AND user_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, trimmedName);
            statement.setInt(2, id);
            statement.setInt(3, currentUserId);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Category not found.");
            }
            categories.clear();
            getAllCategories();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update category.", e);
        }
    }

    public void deleteCategory(int id) {
        Integer currentUserId = getCurrentUserId();
        if (currentUserId == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        String sql = "DELETE FROM Categories WHERE id = ? AND user_id = ?";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, id);
            statement.setInt(2, currentUserId);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Category not found.");
            }
            categories.clear();
            getAllCategories();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete category.", e);
        }
    }

    private Integer getCurrentUserId() {
        String email = SessionManager.getLoggedInUserEmail();
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        var user = userService.getUserByEmail(email);
        return user == null ? null : user.getId();
    }

    private String normalizeName(String name) {
        return name.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }
}
