package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import model.User;
import service.SessionManager;
import service.UserService;
import util.WindowManager;

public class DashboardController {

    @FXML
    private Label welcomeLabel;

    @FXML
    private StackPane contentArea;

    @FXML
    private VBox homeContent;

    private String userEmail;
    private final UserService userService = new UserService();

    @FXML
    public void initialize() {
        welcomeLabel.setText("Welcome to Dashboard!");
        showHomeContent();
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
        loadContent("/fxml/categories.fxml");
    }

    @FXML
    protected void onTransactionsButtonClick(ActionEvent event) {
        loadContent("/fxml/transactions.fxml");
    }

    @FXML
    protected void onReportsButtonClick(ActionEvent event) {
        loadContent("/fxml/reports.fxml");
    }

    @FXML
    protected void onLogoutButtonClick(ActionEvent event) {
        SessionManager.clearSession();
        WindowManager.switchToLogin(event);
    }

    private void showHomeContent() {
        if (contentArea != null && homeContent != null) {
            contentArea.getChildren().setAll(homeContent);
        }
    }

    private void loadContent(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent loadedContent = loader.load();

            contentArea.getChildren().setAll(loadedContent);
        } catch (Exception e) {
            WindowManager.showErrorAlert("Navigation Error", "Unable to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }
}
