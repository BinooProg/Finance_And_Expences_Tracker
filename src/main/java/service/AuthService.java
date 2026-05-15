package service;

import util.DatabaseConnection;
import util.HashUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    private final Connection con;

    public AuthService() {
        this.con = DatabaseConnection.getDatabaseConnection().getConnection();
    }

    public boolean createUser(String firstName, String lastName, String email, String rawPassword) {
        if (isBlank(firstName) || isBlank(lastName) || isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("First name, last name, email and password are required.");
        }

        if (emailExists(email)) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        String sql = "INSERT INTO Users (first_name, last_name, email, password_hash) VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setString(1, firstName.trim());
            ps.setString(2, lastName.trim());
            ps.setString(3, email.trim());
            ps.setString(4, HashUtil.md5(rawPassword));

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            throw new RuntimeException("Failed to create user in database.", e);
        }
    }

    public boolean validateUser(String email, String rawPassword) {
        if (isBlank(email) || isBlank(rawPassword)) {
            throw new IllegalArgumentException("Email and password are required.");
        }

        String sql = "SELECT password_hash FROM Users WHERE email = ?";
        String inputHash = HashUtil.md5(rawPassword);

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String storedHash = rs.getString("password_hash");
                return inputHash.equals(storedHash);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to validate user from database.", e);
        }

        return false;
    }

    private boolean emailExists(String email) {
        String sql = "SELECT id FROM Users WHERE email = ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email.trim());

            ResultSet rs = ps.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to check email existence.", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}