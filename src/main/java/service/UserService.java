package service;

import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserService {

    private final Connection con;

    public UserService() {
        this.con = DatabaseConnection.getDatabaseConnection().getConnection();
    }

    public User getUserByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        String sql = "SELECT * FROM Users WHERE email = ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setString(1, email.trim());

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new User(
                        rs.getInt("id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("password_hash")
                );
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load user from database.", e);
        }

        return null;
    }
}