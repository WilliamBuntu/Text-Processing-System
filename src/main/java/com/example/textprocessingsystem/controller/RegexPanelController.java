package com.example.textprocessingsystem.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

public class RegexPanelController {

    @FXML private TextArea inputTextArea;
    @FXML private TextArea resultTextArea;
    @FXML private TextField regexField;
    @FXML private TextField replacementField;
    @FXML private Button searchButton;
    @FXML private Button replaceButton;
    @FXML private Button loadFileButton;
    @FXML private Button saveFileButton;

    private MainController mainController;

    /**
     * Sets the main controller for status updates.
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }

    /**
     * Handles the search button action.
     */
    @FXML
    private void handleSearch() {
        String text = inputTextArea.getText();
        String regex = regexField.getText();

        if (text.isEmpty() || regex.isEmpty()) {
            showStatus("Input text and regex pattern are required");
            return;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);

            StringBuilder result = new StringBuilder();
            boolean found = false;

            while (matcher.find()) {
                found = true;
                result.append("Match found at positions ")
                        .append(matcher.start())
                        .append("-")
                        .append(matcher.end())
                        .append(": ")
                        .append(matcher.group())
                        .append("\n");
            }

            if (!found) {
                result.append("No matches found.");
            }

            resultTextArea.setText(result.toString());
            showStatus("Search completed");

        } catch (PatternSyntaxException ex) {
            showStatus("Invalid regex pattern: " + ex.getMessage());
            resultTextArea.setText("Error: " + ex.getMessage());
        }
    }

    /**
     * Handles the replace button action.
     */
    @FXML
    private void handleReplace() {
        String text = inputTextArea.getText();
        String regex = regexField.getText();
        String replacement = replacementField.getText();

        if (text.isEmpty() || regex.isEmpty()) {
            showStatus("Input text and regex pattern are required");
            return;
        }

        try {
            Pattern pattern = Pattern.compile(regex);
            Matcher matcher = pattern.matcher(text);
            String result = matcher.replaceAll(replacement);

            resultTextArea.setText(result);
            showStatus("Replace operation completed");

        } catch (PatternSyntaxException ex) {
            showStatus("Invalid regex pattern: " + ex.getMessage());
            resultTextArea.setText("Error: " + ex.getMessage());
        }
    }

    /**
     * Handles loading a file.
     */
    @FXML
    private void handleLoadFile() {
        String fileContent = loadFile(null);
        if (fileContent != null) {
            inputTextArea.setText(fileContent);
            showStatus("File loaded successfully");
        }
    }

    /**
     * Handles saving results to a file.
     */
    @FXML
    private void handleSaveFile() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save Results");
        fileChooser.getExtensionFilters().add(
                new FileChooser.ExtensionFilter("Text Files", "*.txt")
        );

        File file = fileChooser.showSaveDialog(null);
        if (file != null) {
            try {
                writeFile(file, resultTextArea.getText());
                showStatus("Results saved to " + file.getName());
            } catch (IOException ex) {
                showStatus("Error saving file: " + ex.getMessage());
            }
        }
    }

    private String loadFile(File file) {
        if (file == null) {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Open Text File");
            fileChooser.getExtensionFilters().addAll(
                    new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                    new FileChooser.ExtensionFilter("All Files", "*.*")
            );
            file = fileChooser.showOpenDialog(null);
        }

        if (file != null) {
            try {
                return readFile(file);
            } catch (IOException e) {
                showStatus("Error reading file: " + e.getMessage());
                return null;
            }
        }
        return null;
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