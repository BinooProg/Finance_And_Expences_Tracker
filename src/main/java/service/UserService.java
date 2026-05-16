package service;

import model.User;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {
    public User getUserByEmail(String email) {
        String sql = """
                SELECT id, first_name, last_name, email, password_hash
                FROM Users
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email == null ? null : email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return new User(
                            resultSet.getInt("id"),
                            resultSet.getString("first_name"),
                            resultSet.getString("last_name"),
                            resultSet.getString("email"),
                            resultSet.getString("password_hash")
                    );
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to fetch user by email", e);
        }
        return null;
    }

    public boolean updateUser(User user) {
        if (user == null || user.getId() <= 0) {
            throw new IllegalArgumentException("Valid user is required");
        }

        String sql = """
                UPDATE Users
                SET first_name = ?, last_name = ?, email = ?, password_hash = ?
                WHERE id = ?
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, user.getFirstName());
            statement.setString(2, user.getLastName());
            statement.setString(3, user.getEmail());
            statement.setString(4, user.getPasswordHash());
            statement.setInt(5, user.getId());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update user", e);
        }
    }

    public boolean deleteUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required");
        }

        String sql = "DELETE FROM Users WHERE LOWER(email) = LOWER(?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete user", e);
        }
    }
}
