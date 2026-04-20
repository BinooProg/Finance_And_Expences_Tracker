package util;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.event.ActionEvent;
import javafx.scene.Node;

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
            
            // Preserve current window size if it exists
            double currentWidth = stage.getWidth();
            double currentHeight = stage.getHeight();
            
            // Use current size or default if window is not properly sized
            double targetWidth = (currentWidth > 0) ? currentWidth : DEFAULT_WIDTH;
            double targetHeight = (currentHeight > 0) ? currentHeight : DEFAULT_HEIGHT;
            
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            
            // Set consistent window properties
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
            
            // Only resize if current size is invalid
            if (currentWidth <= 0 || currentHeight <= 0) {
                stage.setWidth(targetWidth);
                stage.setHeight(targetHeight);
            }
            
            stage.show();
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
            Scene scene = new Scene(root);
            
            stage.setScene(scene);
            stage.setTitle(title);
            stage.setWidth(width);
            stage.setHeight(height);
            stage.setMinWidth(MIN_WIDTH);
            stage.setMinHeight(MIN_HEIGHT);
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
    }
}
