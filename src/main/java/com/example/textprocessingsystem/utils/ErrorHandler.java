package com.example.textprocessingsystem.utils;

import javafx.scene.control.Alert;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ErrorHandler {
    private static final Logger LOGGER = Logger.getLogger(ErrorHandler.class.getName());

    public static void handleException(String userMessage, Exception e) {
        // Log the error
        LOGGER.log(Level.SEVERE, e.getMessage(), e);

        // Show a user-friendly message
        showErrorToUser(userMessage);
    }

    private static void showErrorToUser(String message) {
        // This is a placeholder for the actual UI error display logic.
        // For example, if using JavaFX:
         Alert alert = new Alert(Alert.AlertType.ERROR);
         alert.setTitle("Error");
         alert.setHeaderText(null);
         alert.setContentText(message);
         alert.showAndWait();

        System.err.println("Error: " + message);
    }
}