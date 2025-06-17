package com.example.textprocessingsystem.controller;

import com.example.textprocessingsystem.regex.RegexProcessor;
import com.example.textprocessingsystem.utils.ErrorHandler;
import com.example.textprocessingsystem.utils.GlobalAlert;
import com.example.textprocessingsystem.utils.CustomLogger;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.*;
import java.util.List;
import java.util.regex.PatternSyntaxException;
import javafx.scene.Node;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.scene.paint.Color;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.jetbrains.annotations.NotNull;

import java.util.Comparator;

public class RegexPanelController {
    private static final Logger logger = CustomLogger.createLogger(RegexPanelController.class.getName());


    @FXML private TextArea inputTextArea;
    ;
    @FXML private TextField regexField;
    @FXML private TextField replacementField;
    @FXML private Button searchButton;
    @FXML private Button replaceButton;
    @FXML private Button loadFileButton;
    @FXML private Button saveFileButton;
    @FXML private Button splitButton;
    @FXML private Button matchesButton;
@FXML private Button replaceFirstButton;
@FXML private TextFlow resultTextArea;
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
            logger.warning("Input text and regex pattern are empty.");
            GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
            showStatus("Input text and regex pattern are required");
            return;
        }

        try {
            List<RegexProcessor.Match> matches = regexProcessor.findMatches(text, regex);
            resultTextArea.getChildren().clear();

            if (matches.isEmpty()) {
                Text noMatches = new Text("No matches found.");
                noMatches.setFill(Color.RED);

                resultTextArea.getChildren().add(noMatches);
            } else {
                // Add header text showing match count
                Text header = new Text("Found " + matches.size() + " match(es):\n\n");

                resultTextArea.getChildren().add(header);

                // Sort matches by start position to process in sequence
                matches.sort(Comparator.comparing(RegexProcessor.Match::getStart));

                int lastIndex = 0;
                for (RegexProcessor.Match match : matches) {
                    int start = match.getStart();
                    int end = match.getEnd();

                    // Add text before the match
                    if (start > lastIndex) {
                        Text beforeMatch = new Text(text.substring(lastIndex, start));
                        resultTextArea.getChildren().add(beforeMatch);
                    }

                    // Add the highlighted match
                    final var highlightedText = getHighlightedText(text, start, end);
                    resultTextArea.getChildren().add(highlightedText);

                    lastIndex = end;
                }

                // Add any remaining text after the last match
                if (lastIndex < text.length()) {
                    Text afterLastMatch = new Text(text.substring(lastIndex));
                    resultTextArea.getChildren().add(afterLastMatch);
                }
            }

            logger.info("Find matches operation completed");
            showStatus("Find matches operation completed");

        } catch (PatternSyntaxException ex) {
            logger.log(Level.SEVERE, "Invalid regex pattern: " + ex.getMessage(), ex);
            showStatus("Invalid regex pattern: " + ex.getMessage());
            Text errorText = new Text("Error: " + ex.getMessage());
            errorText.setFill(Color.RED);
            resultTextArea.getChildren().clear();
            resultTextArea.getChildren().add(errorText);
        }
    }

    @NotNull
    private static Text getHighlightedText(String text, int start, int end) {
        Text highlightedText = new Text(text.substring(start, end));
        highlightedText.setFill(Color.BLACK);
        highlightedText.setStyle(   "-fx-background-color: #CCFFCC;" +  // Light green background
                        "-fx-font-weight: bold;" +         // Bold text
                        "-fx-font-style: italic;" +        // Italic text
                        "-fx-font-size: 17px;" +           // Custom font size
                        "-fx-border-color: #00AA00;" +     // Green border
                        "-fx-border-width: 1px;" +
                  "-fx-background-color: yellow;");        // Border width   );
        return highlightedText;
    }

    private String getTextFlowContent() {
        StringBuilder sb = new StringBuilder();
        for (Node node : resultTextArea.getChildren()) {
            if (node instanceof Text) {
                sb.append(((Text) node).getText());
            }
        }
        return sb.toString();
    }

    private void setTextFlowContent(String text) {
        resultTextArea.getChildren().clear();
        resultTextArea.getChildren().add(new Text(text));
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
            logger.warning("Input text and regex pattern are empty.");
            GlobalAlert.showAlert( Alert.AlertType.INFORMATION, "Input Required", "Please enter text, regex pattern, and replacement string.");
            showStatus("Input text and regex pattern are required");
            return;
        }

        try {
            String result = regexProcessor.replaceAll(text, regex, replacement);
            if (result.isEmpty() || result.equals(text)) {
                setTextFlowContent("No matches found for you Regex hence No replacements made.");
//                resultTextArea.getChildren().add(new Text("No matches found for you Regex hence No replacements made."));
            } else {
                setTextFlowContent(result);
            }
            logger.info("Replace operation completed");
            showStatus("Replace operation completed");

        } catch (PatternSyntaxException ex) {
            logger.log(Level.SEVERE, "Invalid regex pattern: " + ex.getMessage(), ex);
            showStatus("Invalid regex pattern: " + ex.getMessage());
            resultTextArea.getChildren().add(new Text("Error: " + ex.getMessage()));
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
            logger.warning("Input text and regex pattern are empty.");
            GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter text, regex pattern, and replacement string.");
            showStatus("Input text and regex pattern are required.");
            return;
        }

        try {
            String result = regexProcessor.replaceFirst(text, regex, replacement);
            if (result.isEmpty() || result.equals(text)) {
                resultTextArea.getChildren().add(new Text("No matches found for you Regex hence No replacements made."));
            } else {
                setTextFlowContent(result);
                logger.info("Replace first operation completed successfully.");
                showStatus("Replace first operation completed successfully.");
            }

        } catch (PatternSyntaxException ex) {
            logger.log(Level.SEVERE, "Invalid regex pattern: " + ex.getMessage(), ex);
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
                logger.warning("Input text and regex pattern are empty.");
             GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
             showStatus("Input text and regex pattern are required.");
             return;
         }

         try {
             String[] result = regexProcessor.split(text, regex);
             if (result.length == 0) {
                 setTextFlowContent("No splits were made.");
             } else {
                 setTextFlowContent(String.join("\n\n\n", result));
             }
                logger.info("Split operation completed successfully.");
             showStatus("Split operation completed successfully.");
         } catch (PatternSyntaxException ex) {
                logger.log(Level.SEVERE, "Invalid regex pattern: " + ex.getMessage(), ex);
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
             logger.warning("Input text and regex pattern are empty.");
             GlobalAlert.showAlert(Alert.AlertType.INFORMATION, "Input Required", "Please enter both text and regex pattern.");
             showStatus("Input text and regex pattern are required.");
             return;
         }

         try {
             boolean isMatch = regexProcessor.matches(text, regex);
             if (isMatch) {
                 setTextFlowContent("The text matches the regex pattern.");
//                 resultTextArea.getChildren().add(new Text("The text matches the regex pattern."));
             } else {
                    setTextFlowContent("The text does not match the regex pattern.");
//                 resultTextArea.getChildren().add(new Text("The text does not match the regex pattern."));
             }
             logger.info("Matches operation completed successfully.");
             showStatus("Matches operation completed successfully.");
         } catch (PatternSyntaxException ex) {
             logger.log(Level.SEVERE, "Invalid regex pattern: " + ex.getMessage(), ex);
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
                logger.info("File loaded successfully");
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
        logger.warning("No file selected");
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

                String content = getTextFlowContent();
                writeFile(file, content);
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
            logger.log(Level.SEVERE, "Invalid regex pattern: " + regex);
            GlobalAlert.showAlert(Alert.AlertType.ERROR, "Invalid Regex", "The provided regex pattern is invalid.");
            showStatus("Invalid regex pattern: " + regex);
        }}

}