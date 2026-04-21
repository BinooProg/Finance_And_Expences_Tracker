package util;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class WindowManager {

    private static final double DEFAULT_WIDTH = 1200.0;
    private static final double DEFAULT_HEIGHT = 700.0;
    private static final double MIN_WIDTH = 800.0;
    private static final double MIN_HEIGHT = 500.0;

    public static void switchScene(ActionEvent event, String fxmlPath, String title) {
        try {
            Parent root = FXMLLoader.load(WindowManager.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            applyWindowSettings(stage, root, title);

        } catch (IOException e) {
            System.err.println("Unable to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void switchSceneWithSize(ActionEvent event, String fxmlPath, String title,
                                           double width, double height) {
        try {
            Parent root = FXMLLoader.load(WindowManager.class.getResource(fxmlPath));
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();

            stage.setScene(new Scene(root, width, height));
            stage.setTitle(title);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            stage.centerOnScreen();
            stage.show();

        } catch (IOException e) {
            System.err.println("Unable to load page: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void setDefaultWindowSize(Stage stage) {
        stage.setWidth(DEFAULT_WIDTH);
        stage.setHeight(DEFAULT_HEIGHT);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();
    }

    public static void applyFixedScene(Stage stage, Parent root, String title) {
        applyWindowSettings(stage, root, title);
    }

    private static void applyWindowSettings(Stage stage, Parent root, String title) {
        stage.setScene(new Scene(root, DEFAULT_WIDTH, DEFAULT_HEIGHT));
        stage.setTitle(title);
        stage.setMinWidth(MIN_WIDTH);
        stage.setMinHeight(MIN_HEIGHT);
        stage.centerOnScreen();
        stage.show();
    }
}
