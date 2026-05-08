package controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.beans.property.SimpleObjectProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.event.ActionEvent;
import javafx.util.StringConverter;
import model.Category;
import model.Transaction;
import model.User;
import service.CategoryService;
import service.SessionManager;
import service.TransactionService;
import service.UserService;
import util.WindowManager;

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
    private TextField searchTransactionField;

    @FXML
    private TableView<Transaction> transactionsTable;

    @FXML
    private TableColumn<Transaction, Integer> idColumn;

    @FXML
    private TableColumn<Transaction, Integer> userIdColumn;

    @FXML
    private TableColumn<Transaction, String> categoryIdColumn;

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
    private final ObservableList<Transaction> transactions = FXCollections.observableArrayList();
    private FilteredList<Transaction> filteredTransactions;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(new PropertyValueFactory<>("id"));
        amountColumn.setCellValueFactory(new PropertyValueFactory<>("amount"));
        typeColumn.setCellValueFactory(new PropertyValueFactory<>("type"));
        dateColumn.setCellValueFactory(new PropertyValueFactory<>("date"));

        userIdColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(
                        cellData.getValue().getUser() == null ? null : cellData.getValue().getUser().getId()));

        categoryIdColumn.setCellValueFactory(cellData ->
                new SimpleObjectProperty<>(
                        cellData.getValue().getCategory() == null ? "" : cellData.getValue().getCategory().getName()));

        categoryComboBox.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
        configureCategoryComboBoxDisplay();
        typeComboBox.setItems(FXCollections.observableArrayList("Income", "Expense"));

        bindSearch();
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
    protected void onAddEditTransactionButtonClick() {
        try {
            String email = SessionManager.getLoggedInUserEmail();
            User user = userService.getUserByEmail(email);

            if (user == null) {
                WindowManager.showErrorAlert("Input Error", "No logged-in user found.");
                return;
            }

            double amount = Double.parseDouble(amountField.getText().trim());
            Category category = categoryComboBox.getValue();
            String type = typeComboBox.getValue();
            LocalDate date = transactionDatePicker.getValue();

            if (category == null || type == null || date == null) {
                WindowManager.showErrorAlert("Input Error", "Please complete all fields.");
                return;
            }

            if (amount <= 0) {
                WindowManager.showErrorAlert("Input Error", "Amount must be greater than zero.");
                return;
            }

            if (selectedTransaction == null) {
                transactionService.addTransaction(user, category, amount, type, date.toString());
                WindowManager.showInfoAlert("Success", "Transaction added successfully.");
            } else {
                transactionService.updateTransaction(selectedTransaction.getId(), user, category, amount, type, date.toString());
                WindowManager.showInfoAlert("Success", "Transaction updated successfully.");
            }

            clearForm();
            loadTransactions();

        } catch (NumberFormatException e) {
            WindowManager.showErrorAlert("Input Error", "Amount must be a valid number.");
        } catch (Exception e) {
            String message = e.getMessage();
            WindowManager.showErrorAlert("Input Error",
                    (message == null || message.isBlank()) ? "Unable to save transaction." : message);
        }
    }

    @FXML
    protected void onClearButtonClick() {
        clearForm();
    }

    @FXML
    protected void onTodayButtonClick() {
        transactionDatePicker.setValue(LocalDate.now());
    }

    @FXML
    protected void onBackButtonClick(ActionEvent event) {
        WindowManager.switchToDashboard(event);
    }

    private void loadTransactions() {
        transactions.setAll(transactionService.getAllTransactions());
    }

    private void bindSearch() {
        filteredTransactions = new FilteredList<>(transactions, transaction -> true);
        SortedList<Transaction> sortedTransactions = new SortedList<>(filteredTransactions);
        sortedTransactions.comparatorProperty().bind(transactionsTable.comparatorProperty());
        transactionsTable.setItems(sortedTransactions);

        if (searchTransactionField != null) {
            searchTransactionField.textProperty().addListener((observable, oldValue, newValue) ->
                    applyTransactionFilter(newValue));
        }
    }

    private void applyTransactionFilter(String searchText) {
        String keyword = searchText == null ? "" : searchText.trim().toLowerCase();

        filteredTransactions.setPredicate(transaction -> {
            if (transaction == null) {
                return false;
            }

            if (keyword.isEmpty()) {
                return true;
            }

            String categoryName = transaction.getCategory() == null || transaction.getCategory().getName() == null
                    ? ""
                    : transaction.getCategory().getName().toLowerCase();
            String date = transaction.getDate() == null ? "" : transaction.getDate().toLowerCase();

            return categoryName.contains(keyword) || date.contains(keyword);
        });
    }

    private void clearForm() {
        amountField.clear();
        categoryComboBox.setValue(null);
        typeComboBox.setValue(null);
        transactionDatePicker.setValue(null);
        selectedTransaction = null;
        transactionsTable.getSelectionModel().clearSelection();
    }

    private void configureCategoryComboBoxDisplay() {
        categoryComboBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCategory(item));
            }
        });

        categoryComboBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : formatCategory(item));
            }
        });

        categoryComboBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(Category category) {
                return category == null ? "" : formatCategory(category);
            }

            @Override
            public Category fromString(String string) {
                return null;
            }
        });
    }

    private String formatCategory(Category category) {
        return category.getId() + " - " + category.getName();
    }

}
