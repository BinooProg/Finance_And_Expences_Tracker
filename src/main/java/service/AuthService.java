package service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import util.HashUtil;

public class AuthService {
    private static final String[] DEFAULT_CATEGORIES = {"Food", "Salary", "Transport", "Shopping", "Education"};

    /**
     * Registers a new user by validating input, checking email uniqueness, hashing the password,
     * and creating the user record in the database.
     *
     * @param firstName the user's first name
     * @param lastName the user's last name
     * @param email the user's email address
     * @param rawPassword the user's plain-text password before hashing
     * @return true when the user record is created successfully
     * @throws IllegalArgumentException if any required input is blank or the email already exists
     */
    public boolean createUser(String firstName, String lastName, String email, String rawPassword) {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("First name, last name, email and password are required");
        }
        if (emailExists(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        String hashed = HashUtil.md5(rawPassword);
        int createdUserId = createUserInDatabase(firstName, lastName, email, hashed);
        initializeUserCategoriesInDatabase(createdUserId);
        return true;
    }

    /**
     * Validates user login credentials against stored user records.
     *
     * @param email the email to authenticate
     * @param rawPassword the plain-text password to verify
     * @return true when email exists and password hash matches; false when credentials do not match
     * @throws IllegalArgumentException if email or password is blank
     */
    public boolean validateUser(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("Email and password are required");
        }
        String inputHash = HashUtil.md5(rawPassword);

        String sql = """
                SELECT password_hash
                FROM Users
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return false;
                }
                String storedHash = resultSet.getString("password_hash");
                return storedHash != null && storedHash.equals(inputHash);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate user credentials", e);
        }
    }

    /**
     * Checks whether a user with the provided email already exists.
     *
     * @param email the email to search for
     * @return true if a matching email is found; false otherwise
     */
    private boolean emailExists(String email) {
        String sql = """
                SELECT 1
                FROM Users
                WHERE LOWER(email) = LOWER(?)
                LIMIT 1
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, email.trim());
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email existence", e);
        }
    }


    private int createUserInDatabase(String firstName, String lastName, String email, String hashedPassword) {
        String sql = """
                INSERT INTO Users (first_name, last_name, email, password_hash)
                VALUES (?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, firstName.trim());
            statement.setString(2, lastName.trim());
            statement.setString(3, email.trim());
            statement.setString(4, hashedPassword);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new RuntimeException("Failed to create user record");
            }
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (keys.next()) {
                    return keys.getInt(1);
                }
            }
            throw new RuntimeException("Failed to retrieve generated user id");
        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user in database", e);
        }
    }

    private void initializeUserCategoriesInDatabase(int userId) {
        String sql = "INSERT INTO Categories (user_id, name) VALUES (?, ?)";
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            for (String categoryName : DEFAULT_CATEGORIES) {
                statement.setInt(1, userId);
                statement.setString(2, categoryName);
                statement.addBatch();
            }
            statement.executeBatch();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to initialize user categories", e);
        }
    }

    /**
     * Checks whether a text value is null, empty, or whitespace-only.
     *
     * @param value the value to evaluate
     * @return true if the value is blank; false otherwise
     */
    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }


}
