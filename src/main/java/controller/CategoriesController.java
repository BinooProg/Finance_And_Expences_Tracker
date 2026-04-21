package controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import model.Category;
import service.CategoryService;
import util.WindowManager;

public class CategoriesController {

    private final CategoryService categoryService = new CategoryService();

    @FXML
    private TextField categoryNameField;

    @FXML
    private TableView<Category> categoriesTable;

    @FXML
    private TableColumn<Category, Integer> idColumn;

    @FXML
    private TableColumn<Category, String> nameColumn;

    @FXML
    private Button addEditCategoryButton;
    
    @FXML
    private Label messageLabel;

    @FXML
    private Button clearButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button backButton;

    private Category selectedCategory;

    @FXML
    public void initialize() {
        idColumn.setCellValueFactory(cellData ->
                new SimpleIntegerProperty(cellData.getValue().getId()).asObject());

        nameColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().getName()));

        loadCategories();

        categoriesTable.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            selectedCategory = newValue;

            if (newValue != null) {
                categoryNameField.setText(newValue.getName());
            }
        });
    }

    @FXML
private void onAddEditCategoryButtonClick() {
    String categoryName = categoryNameField.getText();

    try {
        if (selectedCategory == null) {
            categoryService.addCategory(categoryName);
            showMessage("Category added successfully.");
        } else {
            categoryService.updateCategory(selectedCategory.getId(), categoryName);
            showMessage("Category updated successfully.");
        }

        loadCategories();
       

    } catch (IllegalArgumentException ex) {
        showError(ex.getMessage());
    } catch (Exception ex) {
        showError("Failed to save category.");
    }
}

    @FXML
    private void onClearButtonClick() {
        clearForm();
    }

    @FXML
    private void onDeleteButtonClick() {
        if (selectedCategory == null) {
            showError("Please select a category to delete.");
            return;
        }

        String deletedCategoryName = selectedCategory.getName();

        try {
            categoryService.deleteCategory(selectedCategory.getId());
            loadCategories();
            clearForm();
            showInfo("Category \"" + deletedCategoryName + "\" deleted successfully.");
        } catch (IllegalArgumentException ex) {
            showError(ex.getMessage());
        } catch (Exception ex) {
            showError("Failed to delete category.");
        }
    }

    @FXML
    private void onBackButtonClick(ActionEvent event) {
        WindowManager.switchToDashboard(event);
    }

    private void loadCategories() {
        categoriesTable.setItems(FXCollections.observableArrayList(categoryService.getAllCategories()));
    }

    private void clearForm() {
        categoryNameField.clear();
        categoriesTable.getSelectionModel().clearSelection();
        selectedCategory = null;
        messageLabel.setText("");
    }
    private void showMessage(String message) {
    messageLabel.setText(message);
}

    private void showError(String message) {
        WindowManager.showErrorAlert("Error", message);
    }

    private void showInfo(String message) {
        WindowManager.showInfoAlert("Information", message);
    }
    
    
    
    
    
    
    
}
