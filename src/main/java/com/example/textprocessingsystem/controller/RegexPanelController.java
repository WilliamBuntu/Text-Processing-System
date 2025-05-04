package com.example.textprocessingsystem.controller;

import com.example.textprocessingsystem.regex.RegexProcessor;
import com.example.textprocessingsystem.utils.ErrorHandler;
import com.example.textprocessingsystem.utils.GlobalAlert;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.*;
import java.util.List;
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
    @FXML private Button splitButton;
    @FXML private Button matchesButton;
@FXML private Button replaceFirstButton;
    private MainController mainController;
    RegexProcessor regexProcessor = new RegexProcessor();

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
        validateRegex(regex);
        if (text.isEmpty() || regex.isEmpty()) {
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
            showStatus("Input text and regex pattern are required");
            return;
        }


        try {
            List<RegexProcessor.Match> matches = regexProcessor.findMatches(text, regex);

            if (matches.isEmpty()) {
                resultTextArea.setText("No matches found.");
            } else {
                StringBuilder result = new StringBuilder("Matches found:\n\n");
                for (RegexProcessor.Match match : matches) {
                    result.append(match.toString()).append("\n");
                }
                resultTextArea.setText(result.toString());
            }

            showStatus("Find matches operation completed");

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
        validateRegex(regex);

        if (text.isEmpty() || regex.isEmpty()) {
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "Input Required", "Please enter text, regex pattern, and replacement string.");
            showStatus("Input text and regex pattern are required");
            return;
        }

        try {
            String result = regexProcessor.replaceAll(text, regex, replacement);
            if (result.isEmpty() || result.equals(text)) {
                resultTextArea.setText("No matches found for you Regex hence No replacements made.");
            } else {
                resultTextArea.setText(result);
            }
            showStatus("Replace operation completed");

        } catch (PatternSyntaxException ex) {
            showStatus("Invalid regex pattern: " + ex.getMessage());
            resultTextArea.setText("Error: " + ex.getMessage());
        }
    }

    /**
     * Handles the replace first button action.
     */

    @FXML
    private void handleReplaceFirst() {
        String text = inputTextArea.getText();
        String regex = regexField.getText();
        String replacement = replacementField.getText();
        validateRegex(regex);

        if (text.isEmpty() || regex.isEmpty()) {
            GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter text, regex pattern, and replacement string.");
            showStatus("Input text and regex pattern are required.");
            return;
        }

        try {
            String result = regexProcessor.replaceFirst(text, regex, replacement);
            if (result.isEmpty() || result.equals(text)) {
                resultTextArea.setText("No matches found for your Regex hence No replacements made.");
            } else {
                resultTextArea.setText(result);
                showStatus("Replace first operation completed successfully.");
            }

        } catch (PatternSyntaxException ex) {
            GlobalAlert.showAlert(Alert.AlertType.ERROR, "Invalid Regex", "The provided regex pattern is invalid: " + ex.getMessage());
            showStatus("Invalid regex pattern: " + ex.getMessage());
        }
    }


     @FXML
     private void handleSplit() {
         String text = inputTextArea.getText();
         String regex = regexField.getText();
         validateRegex(regex);

         if (text.isEmpty() || regex.isEmpty()) {
             GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
             showStatus("Input text and regex pattern are required.");
             return;
         }

         try {
             String[] result = regexProcessor.split(text, regex);
             if (result.length == 0) {
                 resultTextArea.setText("No splits were made.");
             } else {
                 resultTextArea.setText(String.join("\n\n\n", result));
             }
             showStatus("Split operation completed successfully.");
         } catch (PatternSyntaxException ex) {
             GlobalAlert.showAlert(Alert.AlertType.ERROR, "Invalid Regex", "The provided regex pattern is invalid: " + ex.getMessage());
             showStatus("Invalid regex pattern: " + ex.getMessage());
         }
     }

    // handles matches operation

     @FXML
     private void handleMatches() {
         String text = inputTextArea.getText();
         String regex = regexField.getText();
         validateRegex(regex);

         if (text.isEmpty() || regex.isEmpty()) {
             GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
             showStatus("Input text and regex pattern are required.");
             return;
         }

         try {
             boolean isMatch = regexProcessor.matches(text, regex);
             if (isMatch) {
                 resultTextArea.setText("The entire text matches the regex pattern.");
             } else {
                 resultTextArea.setText("The text does not match the regex pattern.");
             }
             showStatus("Matches operation completed successfully.");
         } catch (PatternSyntaxException ex) {
             GlobalAlert.showAlert(Alert.AlertType.ERROR, "Invalid Regex", "The provided regex pattern is invalid: " + ex.getMessage());
             showStatus("Invalid regex pattern: " + ex.getMessage());
         }
     }
    /**
     * Handles loading a file.
     */
@FXML
private void handleLoadFile() {
    System.out.println("Button clicked");

    // Run FileChooser on the JavaFX Application Thread
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open Text File");
    fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("Text Files", "*.txt"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
    );
    File file = fileChooser.showOpenDialog(null);

    if (file != null) {
        Task<String> loadFileTask = new Task<>() {
            @Override
            protected String call() {
                return loadFile(file); // Pass the selected file
            }
        };

        loadFileTask.setOnSucceeded(event -> {
            String fileContent = loadFileTask.getValue();
            if (fileContent != null) {
                inputTextArea.setText(fileContent);
                showStatus("File loaded successfully");
            }
        });

        loadFileTask.setOnFailed(event -> {
            Throwable exception = loadFileTask.getException();
            showStatus("Error loading file: " + (exception != null ? exception.getMessage() : "Unknown error"));
        });

        Thread thread = new Thread(loadFileTask);
        thread.setName("File Loader Thread");
        thread.start();
    } else {
        System.out.println("No file selected");
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
           System.out.println("Selected file: " + file.getAbsolutePath());
           try {
               return readFile(file);
           } catch (IOException e) {
               System.out.println("Error reading file: " + e.getMessage());
               showStatus("Error reading file: " + e.getMessage());
               return null;
           }
       } else {
           System.out.println("No file selected");
       }
       return null;
   }

   private String readFile(File file) throws IOException {
       System.out.println("Reading file: " + file.getAbsolutePath());
       StringBuilder content = new StringBuilder();
       try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
           String line;
           while ((line = reader.readLine()) != null) {
               content.append(line).append("\n");
           }
       }
       System.out.println("File read successfully");
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

    private void validateRegex(String regex) {
        if (!regexProcessor.isValidRegex(regex)) {
            GlobalAlert.showAlert(Alert.AlertType.ERROR, "Invalid Regex", "The provided regex pattern is invalid.");
            showStatus("Invalid regex pattern: " + regex);
        }}

}