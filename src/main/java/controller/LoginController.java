package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;
import service.SessionManager;
import util.VailEmailUtil;
import util.WindowManager;

import java.io.IOException;

public class LoginController {

    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (!VailEmailUtil.isValidEmailFormat(email)) {
            showError("Incorrect information: invalid email format.");
            return;
        }

        if (password == null || password.trim().isEmpty()) {
            showError("Incorrect information: password is required.");
            return;
        }

        try {
            boolean isValid = authService.validateUser(email, password);

            if (isValid) {
                SessionManager.setLoggedInUserEmail(email);

                FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/dashboard.fxml"));
                Parent root = loader.load();

                DashboardController dashboardController = loader.getController();
                dashboardController.setUserEmail(email);

               Stage stage = (Stage) emailField.getScene().getWindow();
               WindowManager.applyFixedScene(stage, root, "Dashboard");
            } else {
                showError("Incorrect email or password.");
            }

        } catch (IOException e) {
            showError("Unable to load dashboard page.");
            e.printStackTrace();
        } catch (Exception ex) {
            showError("Unable to login. Please try again.");
            ex.printStackTrace();
        }
    }

    @FXML
    protected void onSignupButtonClick(ActionEvent event) {
        try {
            Parent registerRoot = FXMLLoader.load(getClass().getResource("/fxml/signup.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(registerRoot));
            stage.setTitle("Sign Up");
            stage.show();
        } catch (Exception ex) {
            showError("Unable to open register page.");
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
