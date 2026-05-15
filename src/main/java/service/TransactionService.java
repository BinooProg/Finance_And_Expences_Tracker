package service;

import model.Category;
import model.Transaction;
import model.User;
import util.DatabaseConnection;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {

    private final Connection con;
    private final UserService userService = new UserService();

    public TransactionService() {
        this.con = DatabaseConnection.getDatabaseConnection().getConnection();
    }

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            return transactions;
        }

        String sql = """
                SELECT 
                    t.id,
                    t.amount,
                    t.type,
                    t.date,
                    c.id AS category_id,
                    c.name AS category_name
                FROM Transactions t
                JOIN Categories c ON t.category_id = c.id
                WHERE t.user_id = ?
                ORDER BY t.date DESC, t.id DESC
                """;

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, currentUser.getId());

            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                Category category = new Category(
                        rs.getInt("category_id"),
                        rs.getString("category_name")
                );

                Transaction transaction = new Transaction(
                        rs.getInt("id"),
                        currentUser,
                        category,
                        rs.getDouble("amount"),
                        rs.getString("type"),
                        rs.getDate("date").toString()
                );

                transactions.add(transaction);
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions from database.", e);
        }

        return transactions;
    }

    public void addTransaction(User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        String sql = """
                INSERT INTO Transactions (user_id, category_id, amount, type, date)
                VALUES (?, ?, ?, ?, ?)
                """;

        try {
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, user.getId());
            ps.setInt(2, category.getId());
            ps.setDouble(3, amount);
            ps.setString(4, normalizeType(type));
            ps.setDate(5, Date.valueOf(LocalDate.parse(date.trim())));

            ps.executeUpdate();

        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction in database.", e);
        }
    }

    public void updateTransaction(int id, User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        String sql = """
                UPDATE Transactions
                SET category_id = ?, amount = ?, type = ?, date = ?
                WHERE id = ? AND user_id = ?
                """;

        try {
            PreparedStatement ps = con.prepareStatement(sql);

            ps.setInt(1, category.getId());
            ps.setDouble(2, amount);
            ps.setString(3, normalizeType(type));
            ps.setDate(4, Date.valueOf(LocalDate.parse(date.trim())));
            ps.setInt(5, id);
            ps.setInt(6, user.getId());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Transaction not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction in database.", e);
        }
    }

    public void deleteTransaction(int id) {
        User currentUser = getCurrentUser();

        if (currentUser == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        String sql = "DELETE FROM Transactions WHERE id = ? AND user_id = ?";

        try {
            PreparedStatement ps = con.prepareStatement(sql);
            ps.setInt(1, id);
            ps.setInt(2, currentUser.getId());

            int rows = ps.executeUpdate();

            if (rows == 0) {
                throw new IllegalArgumentException("Transaction not found.");
            }

        } catch (SQLException e) {
            throw new RuntimeException("Failed to delete transaction from database.", e);
        }
    }

    public double getTotalIncome() {
        return getAllTransactions().stream()
                .filter(t -> "Income".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    public double getTotalExpenses() {
        return getAllTransactions().stream()
                .filter(t -> "Expense".equalsIgnoreCase(t.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private void validateTransactionData(User user, Category category, double amount, String type, String date) {
        if (user == null || category == null || type == null || date == null) {
            throw new IllegalArgumentException("All transaction fields are required.");
        }

        if (type.trim().isEmpty() || date.trim().isEmpty()) {
            throw new IllegalArgumentException("All transaction fields are required.");
        }

        if (amount <= 0) {
            throw new IllegalArgumentException("Amount must be greater than 0.");
        }

        if (!type.equalsIgnoreCase("Income") && !type.equalsIgnoreCase("Expense")) {
            throw new IllegalArgumentException("Type must be either Income or Expense.");
        }

        try {
            LocalDate.parse(date.trim());
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid transaction date.");
        }
    }

    private String normalizeType(String type) {
        return type.trim().equalsIgnoreCase("Income") ? "Income" : "Expense";
    }

    private User getCurrentUser() {
        String email = SessionManager.getLoggedInUserEmail();

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userService.getUserByEmail(email);
    }
}