package controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import service.AuthService;
import util.VailEmailUtil;

public class LoginController {
    private static final String ERROR_STYLE_CLASS = "error-text";
    private static final String SUCCESS_STYLE_CLASS = "success-text";

    private final AuthService authService = new AuthService();

    @FXML
    private TextField emailField;

    @FXML
    private PasswordField passwordField;

    @FXML
    private Label errorLabel;

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
                showSuccess("Login successful.");
            } else {
                showError("Incorrect email or password.");
            }
        } catch (Exception ex) {
            showError("Unable to login. Please try again.");
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
