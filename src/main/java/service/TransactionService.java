package service;

import model.Category;
import model.Transaction;
import model.User;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;

public class TransactionService {
    private static final String TRANSACTIONS_DIRECTORY = "src/main/resources/data/transactions";

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

        if (loadedUserId == null || !loadedUserId.equals(currentUser.getId())) {
            transactions.clear();
            loadedUserId = currentUser.getId();
        }

        if (!transactions.isEmpty()) {
            return transactions;
        }

        Path path = getUserTransactionsPath(currentUser.getId());

        try {
            ensureFileExists(path);

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Category> categories = categoryService.getAllCategories();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\|");
                    if (parts.length < 5) {
                        continue;
                    }

                    int id = Integer.parseInt(parts[0].trim());
                    int categoryId = Integer.parseInt(parts[1].trim());
                    double amount = Double.parseDouble(parts[2].trim());
                    String type = parts[3].trim();
                    String date = parts[4].trim();

                    User user = currentUser;
                    Category category = getCategoryById(categoryId, categories);

                    if (user != null && category != null) {
                        transactions.add(new Transaction(id, user, category, amount, type, date));
                    }
                } catch (NumberFormatException ex) {
                    // Skip malformed row
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return transactions;
    }

    public void addTransaction(User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        int nextId = getNextId();
        String row = nextId + "|" +
                category.getId() + "|" +
                amount + "|" +
                type.trim() + "|" +
                date.trim();

        try {
            Path path = getUserTransactionsPath(user.getId());
            ensureFileExists(path);
            Files.writeString(
                    path,
                    row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );
            if (loadedUserId != null && loadedUserId.equals(user.getId())) {
                transactions.add(new Transaction(nextId, user, category, amount, type.trim(), date.trim()));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to save transaction.", e);
        }
    }

    public void updateTransaction(int id, User user, Category category, double amount, String type, String date) {
        validateTransactionData(user, category, amount, type, date);

        List<Transaction> transactions = getAllTransactions();
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

        rewriteAll(transactions);
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

    private void rewriteAll(List<Transaction> transactions) {
        List<String> rows = new ArrayList<>();

        for (Transaction t : transactions) {
            rows.add(
                    t.getId() + "|" +
                            t.getCategory().getId() + "|" +
                            t.getAmount() + "|" +
                            t.getType() + "|" +
                            t.getDate()
            );
        }

        try {
            User currentUser = getCurrentUser();
            if (currentUser == null) {
                throw new IllegalStateException("No logged-in user found.");
            }
            Files.write(
                    getUserTransactionsPath(currentUser.getId()),
                    rows,
                    StandardCharsets.UTF_8,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.CREATE
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to update transactions.", e);
        }
    }

    private int getNextId() {
        int max = 0;

        for (Transaction t : getAllTransactions()) {
            if (t.getId() > max) {
                max = t.getId();
            }
        }

        return max + 1;
    }

    private Category getCategoryById(int categoryId, List<Category> categories) {
        for (Category c : categories) {
            if (c.getId() == categoryId) {
                return c;
            }
        }
        return null;
    }

    private User getCurrentUser() {
        String email = SessionManager.getLoggedInUserEmail();

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        return userService.getUserByEmail(email);
    }

    private Path getUserTransactionsPath(int userId) {
        return Paths.get(TRANSACTIONS_DIRECTORY, userId + "_transactions.txt");
    }

    private void ensureFileExists(Path path) throws IOException {
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }
}
