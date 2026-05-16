package service;

import model.Category;
import model.Transaction;
import model.User;

import java.util.ArrayList;
import java.util.List;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TransactionService {
    private final List<Transaction> transactions = new ArrayList<>();
    private Integer loadedUserId;
    private final UserService userService = new UserService();
    private final CategoryService categoryService = new CategoryService();

    public List<Transaction> getAllTransactions() {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            transactions.clear();
            loadedUserId = null;
            return transactions;
        }

        transactions.clear();
        loadedUserId = currentUser.getId();

        String sql = """
                SELECT t.id, t.amount, t.type, t.date, c.id AS category_id, c.name AS category_name
                FROM Transactions t
                JOIN Categories c ON c.id = t.category_id
                WHERE t.user_id = ?
                ORDER BY t.id
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, currentUser.getId());
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Category category = new Category(
                            resultSet.getInt("category_id"),
                            resultSet.getString("category_name")
                    );
                    transactions.add(new Transaction(
                            resultSet.getInt("id"),
                            currentUser,
                            category,
                            resultSet.getDouble("amount"),
                            resultSet.getString("type"),
                            resultSet.getDate("date").toString()
                    ));
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to load transactions.", e);
        }

        return transactions;
    }

    public void addTransaction(User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        String sql = """
                INSERT INTO Transactions (user_id, category_id, amount, type, date)
                VALUES (?, ?, ?, ?, ?)
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, user.getId());
            statement.setInt(2, category.getId());
            statement.setDouble(3, amount);
            statement.setString(4, type.trim());
            statement.setDate(5, Date.valueOf(date.trim()));
            statement.executeUpdate();
            if (loadedUserId != null && loadedUserId.equals(user.getId())) {
                transactions.clear();
                getAllTransactions();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Failed to save transaction.", e);
        }
    }

    public void updateTransaction(int id, User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        Integer currentUserId = user == null ? null : user.getId();
        if (currentUserId == null) {
            throw new IllegalStateException("No logged-in user found.");
        }

        getAllTransactions();
        boolean found = false;

        for (Transaction t : transactions) {
            if (t.getId() == id) {
                t.setUser(user);
                t.setCategory(category);
                t.setAmount(amount);
                t.setType(type.trim());
                t.setDate(date.trim());
                found = true;
                break;
            }
        }

        if (!found) {
            throw new IllegalArgumentException("Transaction not found.");
        }

        String sql = """
                UPDATE Transactions
                SET category_id = ?, amount = ?, type = ?, date = ?
                WHERE id = ? AND user_id = ?
                """;
        try (Connection connection = DatabaseConfig.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, category.getId());
            statement.setDouble(2, amount);
            statement.setString(3, type.trim());
            statement.setDate(4, Date.valueOf(date.trim()));
            statement.setInt(5, id);
            statement.setInt(6, currentUserId);
            int rows = statement.executeUpdate();
            if (rows == 0) {
                throw new IllegalArgumentException("Transaction not found.");
            }
            transactions.clear();
            getAllTransactions();
        } catch (SQLException e) {
            throw new RuntimeException("Failed to update transaction.", e);
        }
    }

    public double getTotalIncome() {
        double sum = 0;

        for (Transaction t : getAllTransactions()) {
            if ("Income".equalsIgnoreCase(t.getType())) {
                sum += t.getAmount();
            }
        }

        return sum;
    }

    public double getTotalExpenses() {
        double sum = 0;

        for (Transaction t : getAllTransactions()) {
            if ("Expense".equalsIgnoreCase(t.getType())) {
                sum += t.getAmount();
            }
        }

        return sum;
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
    }

    private User getCurrentUser() {
        String email = SessionManager.getLoggedInUserEmail();

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userService.getUserByEmail(email);
    }
}
