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
    private static final String FILE_PATH = "src/main/resources/data/transactions.txt";

    private final UserService userService = new UserService();
    private final CategoryService categoryService = new CategoryService();

    public List<Transaction> getAllTransactions() {
        List<Transaction> transactions = new ArrayList<>();
        Path path = Paths.get(FILE_PATH);

        try {
            ensureFileExists();

            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<Category> categories = categoryService.getAllCategories();

            for (String line : lines) {
                if (line == null || line.isBlank()) {
                    continue;
                }

                try {
                    String[] parts = line.split("\\|");
                    if (parts.length < 6) {
                        continue;
                    }

                    int id = Integer.parseInt(parts[0].trim());
                    int userId = Integer.parseInt(parts[1].trim());
                    int categoryId = Integer.parseInt(parts[2].trim());
                    double amount = Double.parseDouble(parts[3].trim());
                    String type = parts[4].trim();
                    String date = parts[5].trim();

                    User user = getUserById(userId);
                    Category category = getCategoryById(categoryId, categories);

                    if (user != null && category != null) {
                        transactions.add(new Transaction(id, user, category, amount, type, date));
                    }
                } catch (NumberFormatException ex) {
                    // تجاهل السطر التالف بدل ما يوقف تحميل كل البيانات
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
                user.getId() + "|" +
                category.getId() + "|" +
                amount + "|" +
                type.trim() + "|" +
                date.trim();

        try {
            ensureFileExists();
            Files.writeString(
                    Paths.get(FILE_PATH),
                    row + System.lineSeparator(),
                    StandardCharsets.UTF_8,
                    StandardOpenOption.APPEND
            );
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
                            t.getUser().getId() + "|" +
                            t.getCategory().getId() + "|" +
                            t.getAmount() + "|" +
                            t.getType() + "|" +
                            t.getDate()
            );
        }

        try {
            Files.write(
                    Paths.get(FILE_PATH),
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

    private User getUserById(int userId) {
        String email = SessionManager.getLoggedInUserEmail();

        if (email == null || email.trim().isEmpty()) {
            return null;
        }

        User currentUser = userService.getUserByEmail(email);

        if (currentUser != null && currentUser.getId() == userId) {
            return currentUser;
        }

        return null;
    }

    private Category getCategoryById(int categoryId, List<Category> categories) {
        for (Category c : categories) {
            if (c.getId() == categoryId) {
                return c;
            }
        }
        return null;
    }

    private void ensureFileExists() throws IOException {
        Path path = Paths.get(FILE_PATH);
        Path parent = path.getParent();

        if (parent != null && !Files.exists(parent)) {
            Files.createDirectories(parent);
        }

        if (!Files.exists(path)) {
            Files.createFile(path);
        }
    }
}