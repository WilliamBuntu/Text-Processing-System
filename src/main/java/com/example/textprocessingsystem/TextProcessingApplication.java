package com.example.textprocessingsystem;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import java.io.IOException;

/**
 * Main application class for the Text Processing System.
 * Loads the main UI from FXML and coordinates the application startup.
 */
public class TextProcessingApplication extends Application {

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
            System.out.println("Application started successfully");
        } catch (IOException e) {
            System.err.println("Failed to load application UI");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("Unexpected error during application startup");
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