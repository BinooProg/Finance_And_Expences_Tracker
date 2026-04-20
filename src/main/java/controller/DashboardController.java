package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import model.User;
import service.SessionManager;
import service.UserService;
import util.WindowManager;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private Button categoriesButton;

    @FXML
    private Button transactionsButton;

    @FXML
    private Button reportsButton;

    @FXML
    private Button logoutButton;

    private String userEmail;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Dashboard!");
    }

    public void setUserEmail(String email) {
        this.userEmail = email;
        updateWelcomeLabel();
    }

    private void updateWelcomeLabel() {
        if (userEmail != null && !userEmail.isEmpty()) {
            User user = userService.getUserByEmail(userEmail);

            if (user != null && user.getFirstName() != null && !user.getFirstName().trim().isEmpty()) {
                welcomeLabel.setText("Welcome, " + user.getFirstName().trim() + "!");
            } else {
                welcomeLabel.setText("Welcome to Dashboard!");
            }
        } else {
            welcomeLabel.setText("Welcome to Dashboard!");
        }
    }

    @FXML
    protected void onCategoriesButtonClick(ActionEvent event) {
        switchScene(event, "/fxml/categories.fxml", "Categories");
    }

    @FXML
    protected void onTransactionsButtonClick(ActionEvent event) {
        switchScene(event, "/fxml/transactions.fxml", "Transactions");
    }

    @FXML
    protected void onReportsButtonClick(ActionEvent event) {
        switchScene(event, "/fxml/reports.fxml", "Reports");
    }

    @FXML
    protected void onLogoutButtonClick(ActionEvent event) {
        SessionManager.clearSession();
        switchScene(event, "/fxml/login.fxml", "Login");
    }

    private void switchScene(ActionEvent event, String fxmlPath, String title) {
        WindowManager.switchScene(event, fxmlPath, title);
    }
}