package com.example.textprocessingsystem.controller;
import com.example.textprocessingsystem.analysisPackage.BatchProcessor;
import com.example.textprocessingsystem.utils.GlobalAlert;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import java.util.logging.Level;
import java.util.logging.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class BatchProcessingController {
    private static final Logger logger = Logger.getLogger(BatchProcessingController.class.getName());

    @FXML
    private ListView<String> fileListView;

    @FXML
    private TextField regexField;

    @FXML
    private TextField replacementField;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private Button addFilesButton;

    @FXML
    private Button processFilesButton;

    @FXML
    private Button saveResultsButton;
    @FXML
    private Button batchFindReplaceButton;

    private MainController mainController;
    private final List<File> files = new ArrayList<>();

    /**
     * Sets the main controller for status updates.
     *
     * @param mainController The main controller
     */
    public void setMainController(MainController mainController) {

        this.mainController = mainController;
    }

    /**
     * Handles adding files to the batch list.
     */
    @FXML
    private void handleAddFiles() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Files for Batch Processing");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("All Files", "*.*")
        );

        List<File> selectedFiles = fileChooser.showOpenMultipleDialog(null);
        if (selectedFiles != null) {
            files.addAll(selectedFiles);
            for (File file : selectedFiles) {
                fileListView.getItems().add(file.getName());
            }
            showStatus("Files added successfully.");
        }
    }

    /**
     * Handles processing files with the given regex and replacement.
     */
    @FXML
    private void handleProcessFiles() {
        String regex = regexField.getText();
        String replacement = replacementField.getText();

        if (regex.isEmpty()) {
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "Regex pattern required", "Please enter a regex pattern.");
            logger.warning("Regex pattern is empty.");
            showStatus("Regex pattern is required for processing.");
            return;
        }

        StringBuilder results = new StringBuilder();
        for (File file : files) {
            try {
                String content = readFile(file);
                String processedContent = content.replaceAll(regex, replacement);
                results.append("File: ").append(file.getName()).append("\n")
                        .append(processedContent).append("\n\n");
                logger.info("File processed successfully: " + file.getName());
            } catch (IOException e) {
                String errorMessage = "Error processing file " + file.getName() + ": " + e.getMessage();
                results.append(errorMessage).append("\n\n");
                logger.log(Level.SEVERE, errorMessage, e);
            }
        }

        resultTextArea.setText(results.toString());
        logger.info("Batch processing completed.");
        showStatus("Batch processing completed.");
    }

    /**
     * Handles saving the results to a file.
     */
    @FXML
    private void handleSaveResults() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Batch Results");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                writeFile(file, resultTextArea.getText());
                logger.info("Results saved to file: " + file.getName());
                showStatus("Results saved to " + file.getName());
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error saving results: " + e.getMessage(), e);
                showStatus("Error saving results: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBatchFindReplace() {
        if (files.isEmpty()) {
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "No files selected", "Please add files to process.");
            logger.warning("No files selected for batch processing.");
            showStatus("No files selected for batch processing.");
            return;
        }

        String regex = regexField.getText();
        String replacement = replacementField.getText();

        if (regex.isEmpty()) {
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "Regex pattern required", "Please enter a regex pattern.");
            logger.warning("Regex pattern is empty.");
            showStatus("Regex pattern is required for find and replace operation.");
            return;
        }

        // Create directory chooser for output files
        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter("Output Directory", "*.txt");
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Output Directory");

        // Use DirectoryChooser instead since FileChooser can't select directories
        File outputDir = new File(System.getProperty("user.home"));
        try {
            // Initialize batch processor
            BatchProcessor batchProcessor = new BatchProcessor();

            // Show progress in the result area
            resultTextArea.clear();
            resultTextArea.appendText("Starting batch find and replace...\n");

            // Process the files
            BatchProcessor.BatchResult result = batchProcessor.batchFindReplace(
                    files,
                    outputDir,
                    regex,
                    replacement,
                    progress -> {
                        // Update UI with progress information
                        String progressMsg = String.format("Processed %d/%d files. %s",
                                progress.getCompleted(), progress.getTotal(), progress.getMessage());

                        // Update UI on JavaFX thread
                        javafx.application.Platform.runLater(() -> {
                            resultTextArea.appendText(progressMsg + "\n");
                            logger.info(progressMsg);
                            showStatus(progressMsg);
                        });
                    }
            );

            // Show final results
            javafx.application.Platform.runLater(() -> {
                resultTextArea.appendText("\n--- COMPLETED ---\n");
                resultTextArea.appendText(result.toString() + "\n");
                resultTextArea.appendText("Files saved to: " + outputDir.getAbsolutePath() + "\n");
                logger.info("Batch find and replace completed. " + result.getSuccessCount() + " files processed successfully.");
                showStatus("Batch find and replace completed. " +
                        result.getSuccessCount() + " files processed successfully.");
            });

        } catch (Exception e) {
            showStatus("Error during batch processing: " + e.getMessage());
            logger.log(Level.SEVERE, "Error during batch processing: " + e.getMessage(), e);
            resultTextArea.appendText("ERROR: " + e.getMessage() + "\n");
        }
    }

    private String readFile(File file) throws IOException {
        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }
        return content.toString();
    }

    private void writeFile(File file, String content) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file))) {
            writer.write(content);
        }
    }

    private void showStatus(String message) {
        if (mainController != null) {
            mainController.showStatus(message);
        }
    }
}
