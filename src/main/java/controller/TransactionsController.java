package controller;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import model.Category;
import model.Transaction;
import model.User;
import service.CategoryService;
import service.SessionManager;
import service.TransactionService;
import service.UserService;

import java.io.IOException;
import java.time.LocalDate;

public class TransactionsController {

    @FXML
    private TextField amountField;

    @FXML
    private ComboBox<Category> categoryComboBox;

    @FXML
    private ComboBox<String> typeComboBox;

    @FXML
    private DatePicker transactionDatePicker;

    @FXML
    private Button addEditTransactionButton;

    @FXML
    private Button clearButton;

    @FXML
    private Button backButton;

    @FXML
    private Label messageLabel;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, Integer> idColumn;

    @FXML
    private TableColumn<Transaction, Integer> userIdColumn;

    @FXML
    private TableColumn<Transaction, Integer> categoryIdColumn;

    @FXML
    private TableColumn<Transaction, Double> amountColumn;

    @FXML
    private TableColumn<Transaction, String> typeColumn;

    @FXML
    private TableColumn<Transaction, String> dateColumn;

    private final TransactionService transactionService = new TransactionService();
    private final CategoryService categoryService = new CategoryService();
    private final UserService userService = new UserService();

    private Transaction selectedTransaction;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        userIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getUser().getId()));

        categoryIdColumn.setCellValueFactory(cellData ->
                new javafx.beans.property.SimpleObjectProperty<>(cellData.getValue().getCategory().getId()));

        categoryComboBox.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));

        loadTransactions();

        transactionsTable.setOnMouseClicked(event -> {
            Transaction transaction = transactionsTable.getSelectionModel().getSelectedItem();
            if (transaction != null) {
                selectedTransaction = transaction;
                amountField.setText(String.valueOf(transaction.getAmount()));
                categoryComboBox.setValue(transaction.getCategory());
                typeComboBox.setValue(transaction.getType());
                transactionDatePicker.setValue(LocalDate.parse(transaction.getDate()));
            }
        });
    }

    @FXML
    protected void addEditTransactionBH() {
        try {
            String email = SessionManager.getLoggedInUserEmail();
            User user = userService.getUserByEmail(email);

            if (user == null) {
                showMessage("No logged-in user found.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            Category category = categoryComboBox.getValue();
            String type = typeComboBox.getValue();
            LocalDate date = transactionDatePicker.getValue();

            if (category == null || type == null || date == null) {
                showMessage("Please complete all fields.");
                return;
            }

            if (selectedTransaction == null) {
                transactionService.addTransaction(user, category, amount, type, date.toString());
                showMessage("Transaction added successfully.");
            } else {
                transactionService.updateTransaction(selectedTransaction.getId(), user, category, amount, type, date.toString());
                showMessage("Transaction updated successfully.");
            }

            clearForm();
            loadTransactions();

        } catch (NumberFormatException e) {
            showMessage("Amount must be a valid number.");
        } catch (Exception e) {
            showMessage(e.getMessage());
        }
    }

    @FXML
    protected void clearBH() {
        clearForm();
        showMessage("");
    }

    @FXML
    protected void backBH(ActionEvent event) {
        switchScene(event, "/fxml/dashboard.fxml", "Dashboard");
    }

    private void loadTransactions() {
        transactionsTable.setItems(FXCollections.observableArrayList(transactionService.getAllTransactions()));
    }

    private void clearForm() {
        amountField.clear();
        categoryComboBox.setValue(null);
        typeComboBox.setValue(null);
        transactionDatePicker.setValue(null);
        selectedTransaction = null;
        transactionsTable.getSelectionModel().clearSelection();
    }

    private void showMessage(String msg) {
        messageLabel.setText(msg);
    }

    private void switchScene(ActionEvent event, String path, String title) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource(path));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}