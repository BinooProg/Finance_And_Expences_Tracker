package model;

public class Transaction {
    private int id;
    private User user;
    private Category category;
    private double amount;
    private String type;
    private String date;

    public Transaction(int id, User user, Category category, double amount, String type, String date) {
        this.id = id;
        this.user = user;
        this.category = category;
        this.amount = amount;
        this.type = type.trim().equalsIgnoreCase("Income") ? "Income" : "Expense";
        this.date = date;
    }

    public int getId() {
        return id;
    }

    public User getUser() {
        return user;
    }

    public Category getCategory() {
        return category;
    }

    public double getAmount() {
        return amount;
    }

    public String getType() {
        return type;
    }

    public String getDate() {
        return date;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setAmount(double amount) {
        this.amount = amount;
    }

    public void setType(String type) {
        this.type = type.trim().equalsIgnoreCase("income") ? "income" : "expense";
    }

    public void setDate(String date) {
        this.date = date;
    }


    @Override
    public String toString() {
        return String.format(
                "Transaction [ID: %d | User: %d | Cat: %d | Amt: $%.2f | Type: %s | Date: %s]",
                id, user.getId(), category.getId(), amount, type, date
        );
    }
}