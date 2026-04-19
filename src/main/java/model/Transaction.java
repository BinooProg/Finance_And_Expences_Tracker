package model;

public class Transaction {
    private int id;
    private User user;
    private Category category;
    private double amount;
    private String type;
    private String date;

    public void displayTransaction() {
        // Format: ID (5 chars), User/Cat IDs (8 chars), Amount (10 chars), Type (8 chars), Date
        String format = "| %-5d | U-%-6d | C-%-6d | %10.2f | %-8s | %-12s |%n";

        System.out.printf(format, id, user.getId(), category.getId(), amount, type, date);
    }

    @Override
    public String toString() {
        return String.format(
                "Transaction [ID: %d | User: %d | Cat: %d | Amt: $%.2f | Type: %s | Date: %s]",
                id, user.getId(), category.getId(), amount, type, date
        );
    }
}
