package controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import service.TransactionService;
import util.WindowManager;

public class ReportsController {
    private static final String BALANCE_POSITIVE_CLASS = "report-balance-positive";
    private static final String BALANCE_NEGATIVE_CLASS = "report-balance-negative";

    @FXML
    private Label totalIncomeValueLabel;

    @FXML
    private Label totalExpensesValueLabel;

    @FXML
    private Label balanceValueLabel;

    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        double income = transactionService.getTotalIncome();
        double expenses = transactionService.getTotalExpenses();
        double balance = income - expenses;

        totalIncomeValueLabel.setText(String.format("%.2f", income));
        totalExpensesValueLabel.setText(String.format("%.2f", expenses));
        balanceValueLabel.setText(String.format("%.2f", balance));
        updateBalanceStyle(balance);
    }

    @FXML
    protected void onBackButtonClick(ActionEvent event) {
        WindowManager.switchToDashboard(event);
    }

    private void updateBalanceStyle(double balance) {
        balanceValueLabel.getStyleClass().removeAll(BALANCE_POSITIVE_CLASS, BALANCE_NEGATIVE_CLASS);
        if (balance < 0) {
            balanceValueLabel.getStyleClass().add(BALANCE_NEGATIVE_CLASS);
        } else {
            balanceValueLabel.getStyleClass().add(BALANCE_POSITIVE_CLASS);
        }
    }
}
