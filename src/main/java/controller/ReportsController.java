package controller;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.collections.FXCollections;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.util.Duration;
import model.Transaction;
import service.TransactionService;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

public class ReportsController {
    private static final String TYPE_INCOME = "income";
    private static final String TYPE_EXPENSE = "expense";
    private static final String UNCATEGORIZED = "Uncategorized";
    private static final String BALANCE_POSITIVE_CLASS = "report-balance-positive";
    private static final String BALANCE_NEGATIVE_CLASS = "report-balance-negative";
    private static final String SORT_INCOME_HIGH_LOW = "Income (High-Low)";
    private static final String SORT_INCOME_LOW_HIGH = "Income (Low-High)";
    private static final String SORT_EXPENSE_HIGH_LOW = "Expenses (High-Low)";
    private static final String SORT_EXPENSE_LOW_HIGH = "Expenses (Low-High)";

    @FXML
    private Label totalIncomeValueLabel;

    @FXML
    private Label totalExpensesValueLabel;

    @FXML
    private Label balanceValueLabel;

    @FXML
    private TableView<CategorySummaryRow> categorySummaryTable;

    @FXML
    private TableColumn<CategorySummaryRow, String> categoryColumn;

    @FXML
    private TableColumn<CategorySummaryRow, String> categoryIncomeColumn;

    @FXML
    private TableColumn<CategorySummaryRow, String> categoryExpenseColumn;

    @FXML
    private TableColumn<CategorySummaryRow, String> categoryNetColumn;

    @FXML
    private ComboBox<String> sortSummaryComboBox;

    private final TransactionService transactionService = new TransactionService();
    private final Timeline reportAutoRefresh = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> refreshReportData())
    );
    private final Comparator<CategorySummaryRow> byCategoryName =
            Comparator.comparing(CategorySummaryRow::categoryName, String.CASE_INSENSITIVE_ORDER);

    @FXML
    public void initialize() {
        configureCategoryTable();
        configureSort();
        refreshReportData();

        reportAutoRefresh.setCycleCount(Timeline.INDEFINITE);
        reportAutoRefresh.play();

        categorySummaryTable.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                reportAutoRefresh.stop();
            }
        });
    }

    private void configureCategoryTable() {
        categoryColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().categoryName()));
        categoryIncomeColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatAmount(cellData.getValue().income())));
        categoryExpenseColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatAmount(cellData.getValue().expenses())));
        categoryNetColumn.setCellValueFactory(cellData -> new SimpleStringProperty(formatAmount(cellData.getValue().net())));
    }

    private void configureSort() {
        sortSummaryComboBox.setItems(FXCollections.observableArrayList(
                SORT_INCOME_HIGH_LOW,
                SORT_EXPENSE_HIGH_LOW,
                SORT_INCOME_LOW_HIGH,
                SORT_EXPENSE_LOW_HIGH
        ));
        sortSummaryComboBox.setValue(SORT_INCOME_HIGH_LOW);
        sortSummaryComboBox.valueProperty().addListener((observable, oldValue, newValue) -> refreshReportData());
    }

    private void refreshReportData() {
        List<Transaction> transactions = transactionService.getAllTransactions();

        double income = sumByType(transactions, TYPE_INCOME);
        double expenses = sumByType(transactions, TYPE_EXPENSE);

        double balance = income - expenses;

        totalIncomeValueLabel.setText(formatAmount(income));
        totalExpensesValueLabel.setText(formatAmount(expenses));
        balanceValueLabel.setText(formatAmount(balance));
        updateBalanceStyle(balance);

        List<CategorySummaryRow> rows = transactions.stream()
                .collect(Collectors.groupingBy(
                        transaction -> transaction.getCategory() == null ? UNCATEGORIZED : transaction.getCategory().getName()
                ))
                .entrySet()
                .stream()
                .map(this::toCategorySummaryRow)
                .collect(Collectors.toList());

        rows.sort(getSummaryComparator(sortSummaryComboBox.getValue()));
        categorySummaryTable.getItems().setAll(rows);
    }

    private Comparator<CategorySummaryRow> getSummaryComparator(String sortOption) {
        if (SORT_EXPENSE_HIGH_LOW.equals(sortOption)) {
            return Comparator.comparingDouble(CategorySummaryRow::expenses).reversed()
                    .thenComparing(byCategoryName);
        }
        if (SORT_INCOME_LOW_HIGH.equals(sortOption)) {
            return Comparator.comparingDouble(CategorySummaryRow::income)
                    .thenComparing(byCategoryName);
        }
        if (SORT_EXPENSE_LOW_HIGH.equals(sortOption)) {
            return Comparator.comparingDouble(CategorySummaryRow::expenses)
                    .thenComparing(byCategoryName);
        }

        return Comparator.comparingDouble(CategorySummaryRow::income).reversed()
                .thenComparing(byCategoryName);
    }

    private CategorySummaryRow toCategorySummaryRow(Map.Entry<String, List<Transaction>> entry) {
        double income = sumByType(entry.getValue(), TYPE_INCOME);
        double expenses = sumByType(entry.getValue(), TYPE_EXPENSE);

        return new CategorySummaryRow(entry.getKey(), income, expenses, income - expenses);
    }

    private double sumByType(List<Transaction> transactions, String type) {
        return transactions.stream()
                .filter(transaction -> type.equalsIgnoreCase(transaction.getType()))
                .mapToDouble(Transaction::getAmount)
                .sum();
    }

    private String formatAmount(double value) {
        return String.format(Locale.ROOT, "%.2f", value);
    }

    private void updateBalanceStyle(double balance) {
        balanceValueLabel.getStyleClass().removeAll(BALANCE_POSITIVE_CLASS, BALANCE_NEGATIVE_CLASS);
        if (balance < 0) {
            balanceValueLabel.getStyleClass().add(BALANCE_NEGATIVE_CLASS);
        } else {
            balanceValueLabel.getStyleClass().add(BALANCE_POSITIVE_CLASS);
        }
    }

    public record CategorySummaryRow(String categoryName, double income, double expenses, double net) {
    }
}
