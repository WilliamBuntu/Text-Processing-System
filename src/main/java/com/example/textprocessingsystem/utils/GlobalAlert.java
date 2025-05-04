package com.example.textprocessingsystem.utils;

import javafx.scene.control.Alert;

public class GlobalAlert {

    /**
     * Displays an alert with the current message, title, and header.
     */
    public static void showAlert( Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(type.toString() + " Alert" );
        alert.setHeaderText(title);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
