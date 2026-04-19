module com.example.finance_and_expences_tracker {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.finance_and_expences_tracker to javafx.fxml;
    exports com.example.finance_and_expences_tracker;
}