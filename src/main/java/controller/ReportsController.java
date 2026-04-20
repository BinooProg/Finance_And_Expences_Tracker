package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.event.ActionEvent;
import javafx.stage.Stage;
import service.TransactionService;

import java.io.IOException;

public class ReportsController {

    @FXML
    private Label totalIncomeLabel;

    @FXML
    private Label totalExpensesLabel;

    @FXML
    private Label balanceLabel;

    @FXML
    private Button backButton;

    private final TransactionService transactionService = new TransactionService();

    @FXML
    public void initialize() {
        double income = transactionService.getTotalIncome();
        double expenses = transactionService.getTotalExpenses();
        double balance = income - expenses;

        totalIncomeLabel.setText(String.format("Total Income: %.2f", income));
        totalExpensesLabel.setText(String.format("Total Expenses: %.2f", expenses));
        balanceLabel.setText(String.format("Balance: %.2f", balance));
    }

    @FXML
    protected void backBH(ActionEvent event) {
        try {
            Parent root = FXMLLoader.load(getClass().getResource("/fxml/dashboard.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(root));
            stage.setTitle("Dashboard");
            stage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}