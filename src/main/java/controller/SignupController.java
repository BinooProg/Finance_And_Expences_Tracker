package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;
import util.VailEmailUtil;

import java.io.IOException;

public class SignupController {
    private static final String ERROR_STYLE_CLASS = "error-text";
    private static final String SUCCESS_STYLE_CLASS = "success-text";

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
    private Label errorLabel;

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
            showSuccess("Account created successfully.");
            clearFields();
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Unable to register. Please try again.");
        }
    }

    @FXML
    protected void onBackButtonClick(ActionEvent event) {
        try {
            Parent loginRoot = FXMLLoader.load(getClass().getResource("/fxml/login.fxml"));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setScene(new Scene(loginRoot));
            stage.setTitle("Finance Tracker");
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
        errorLabel.getStyleClass().remove(SUCCESS_STYLE_CLASS);
        if (!errorLabel.getStyleClass().contains(ERROR_STYLE_CLASS)) {
            errorLabel.getStyleClass().add(ERROR_STYLE_CLASS);
        }
        errorLabel.setText(message);
    }

    private void showSuccess(String message) {
        errorLabel.getStyleClass().remove(ERROR_STYLE_CLASS);
        if (!errorLabel.getStyleClass().contains(SUCCESS_STYLE_CLASS)) {
            errorLabel.getStyleClass().add(SUCCESS_STYLE_CLASS);
        }
        errorLabel.setText(message);
    }
}
