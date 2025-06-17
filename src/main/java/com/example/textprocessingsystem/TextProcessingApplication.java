package com.example.textprocessingsystem;

import com.example.textprocessingsystem.controller.RegexPanelController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;
import java.util.logging.Logger;

import com.example.textprocessingsystem.utils.CustomLogger;

/**
 * Main application class for the Text Processing System.
 * Loads the main UI from FXML and coordinates the application startup.
 */
public class TextProcessingApplication extends Application {
    private static final Logger logger = CustomLogger.createLogger(RegexPanelController.class.getName());


    @Override
    public void start(Stage primaryStage) {
        try {
            // Load the main view from FXML
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/example/textprocessingsystem/MainView.fxml"));
            BorderPane mainLayout = loader.load();

            // Set up the scene with appropriate styling
            Scene scene = new Scene(mainLayout, 1000, 700);
            scene.getStylesheets().add(getClass().getResource("/com/example/textprocessingsystem/application.css").toExternalForm());
            // Configure and display the main window

            primaryStage.setTitle("DataFlow Text Processor");
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(500);
            primaryStage.show();


            // Log application startup
            logger.info("Application started successfully");
        } catch (IOException e) {
            logger.severe("Failed to load application UI");
            e.printStackTrace();
        } catch (Exception e) {
            logger.severe("Unexpected error during application startup");
            e.printStackTrace();
        }
    }

    /**
     * Main method that launches the JavaFX application.
     * @param args Command line arguments
     */
    public static void main(String[] args) {
        launch(args);
    }
}