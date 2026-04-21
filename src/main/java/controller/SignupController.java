package controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;
import util.VailEmailUtil;
import util.WindowManager;

import java.io.IOException;

public class SignupController {
    private final AuthService authService = new AuthService();

    @FXML
    private TextField firstNameField;

    @FXML
    private TextField lastNameField;

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private PasswordField confirmPasswordField;

    @FXML
    protected void onSignupButtonClick() {
        String firstName = firstNameField.getText();
        String lastName = lastNameField.getText();
        String email = emailField.getText();
        String password = passwordField.getText();
        String confirmPassword = confirmPasswordField.getText();

        if (isBlank(firstName) || isBlank(lastName)) {
            showError("First name and last name are required.");
            return;
        }
        if (!VailEmailUtil.isValidEmailFormat(email)) {
            showError("Incorrect information: invalid email format.");
            return;
        }
        if (isBlank(password)) {
            showError("Password is required.");
            return;
        }
        if (password.length() < 6) {
            showError("Password must be at least 6 characters.");
            return;
        }
        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match.");
            return;
        }

        try {
            authService.createUser(firstName.trim(), lastName.trim(), email.trim(), password);
            clearFields();
            goToLoginPage("account registered.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Unable to register. Please try again.");
        }
    }

    @FXML
    protected void onBackButtonClick() {
        try {
            goToLoginPage(null);
        } catch (Exception ex) {
            showError("Unable to open login page.");
        }
    }

    private void goToLoginPage(String successMessage) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/login.fxml"));
            Scene scene = new Scene(loader.load(), WindowManager.AUTH_WIDTH, WindowManager.AUTH_HEIGHT);
            LoginController loginController = loader.getController();
            if (successMessage != null && !successMessage.trim().isEmpty()) {
                loginController.showSuccessMessage(successMessage.trim());
            }

            Stage stage = (Stage) firstNameField.getScene().getWindow();
            stage.setMinWidth(WindowManager.AUTH_WIDTH);
            stage.setMinHeight(WindowManager.AUTH_HEIGHT);
            stage.setScene(scene);
            stage.setTitle("Finance Tracker");
            stage.centerOnScreen();
            stage.show();
        } catch (IOException ex) {
            showError("Unable to open login page.");
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    private void clearFields() {
        firstNameField.clear();
        lastNameField.clear();
        emailField.clear();
        passwordField.clear();
        confirmPasswordField.clear();
    }

    private void showError(String message) {
        WindowManager.showErrorAlert("Error", message);
    }
}
