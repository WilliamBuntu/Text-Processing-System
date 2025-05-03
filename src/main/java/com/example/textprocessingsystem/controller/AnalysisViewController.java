package com.example.textprocessingsystem.controller;


import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
 import java.util.HashMap;
import java.util.LinkedHashMap;
import      java.util.Map;
import java.util.function.Function;
import    java.util.regex.Matcher;
     import    java.util.regex.Pattern;
     import    java.util.regex.PatternSyntaxException;
import java.util.stream.Collectors;

public class AnalysisViewController {

    @FXML
    private TextArea inputTextArea;

    @FXML
    private TextArea resultTextArea;

    @FXML
    private TextField patternField;

    @FXML
    private Button analyzeWordFrequencyButton;

    @FXML
    private Button analyzePatternFrequencyButton;

    @FXML
    private Button generateSummaryButton;
    @FXML
    private Button analyzeCharacterDistributionButton;

    private MainController mainController;

    /**
     * Sets the main controller for status updates.
     *
     * @param mainController The main controller
     */
    public void setMainController(MainController mainController) {
        this.mainController = mainController;
    }



    /**
     * Handles the action for analyzing word frequencies.
     */
    @FXML
    private void handleAnalyzeWordFrequency() {
        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            showStatus("Input text is required for word frequency analysis.");
            return;
        }

        String result = analyzeWordFrequencies(text);
        resultTextArea.setText(result);
        showStatus("Word frequency analysis completed.");
    }

    /**
     * Handles the action for analyzing pattern frequencies.
     */
    @FXML
    private void handleAnalyzePatternFrequency() {
        String text = inputTextArea.getText();
        String pattern = patternField.getText();

        if (text.isEmpty() || pattern.isEmpty()) {
            showStatus("Input text and pattern are required for pattern frequency analysis.");
            return;
        }

        String result = analyzePatternFrequencies(text, pattern);
        resultTextArea.setText(result);
        showStatus("Pattern frequency analysis completed.");
    }

    /**
     * Handles the action for generating a text summary.
     */
    @FXML
    private void handleGenerateSummary() {
        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            showStatus("Input text is required for generating a summary.");
            return;
        }

        String result = generateSummary(text);
        resultTextArea.setText(result);
        showStatus("Text summary generated.");
    }

    /**
     * Analyzes word frequencies in the given text.
     *
     * @param text The input text
     * @return A formatted string of word frequencies
     */
    private String analyzeWordFrequencies(String text) {
        // Example logic for word frequency analysis
        String[] words = text.split("\\s+");
        Map<String, Integer> wordCounts = new HashMap<>();

        for (String word : words) {
            word = word.toLowerCase().replaceAll("[^a-zA-Z0-9]", "");
            wordCounts.put(word, wordCounts.getOrDefault(word, 0) + 1);
        }

        StringBuilder result = new StringBuilder("Word Frequencies:\n");
        wordCounts.forEach((word, count) -> result.append(word).append(": ").append(count).append("\n"));

        return result.toString();
    }

    /**
     * Analyzes pattern frequencies in the given text.
     *
     * @param text The input text
     * @param pattern The regex pattern
     * @return A formatted string of pattern frequencies
     */
    private String analyzePatternFrequencies(String text, String pattern) {
        try {
            Pattern compiledPattern = Pattern.compile(pattern);
            Matcher matcher = compiledPattern.matcher(text);

            int count = 0;
            while (matcher.find()) {
                count++;
            }

            return "Pattern \"" + pattern + "\" found " + count + " times.";
        } catch (PatternSyntaxException e) {
            return "Invalid regex pattern: " + e.getMessage();
        }
    }

    /**
     * Generates a summary of the given text.
     *
     * @param text The input text
     * @return A summary of the text
     */
    private String generateSummary(String text) {
        // Example logic for text summarization
        String[] sentences = text.split("\\.\\s+");
        int summaryLength = Math.min(3, sentences.length);

        StringBuilder summary = new StringBuilder("Summary:\n");
        for (int i = 0; i < summaryLength; i++) {
            summary.append(sentences[i]).append(".\n");
        }

        return summary.toString();
    }

    /**
     * Updates the status in the main controller.
     *
     * @param message The status message
     */
    private void showStatus(String message) {
        if (mainController != null) {
            mainController.showStatus(message);
        }
    }
    /**
     * Analyzes character distribution in the given text.
     *
     * @param text The input text
     * @return A formatted string of character distribution
     */
    @FXML
public void analyzeCharacterDistribution(ActionEvent actionEvent) {
    String text = inputTextArea.getText();
    if (text.isEmpty()) {
        showStatus("Input text is required for character distribution analysis.");
        return;
    }

    // Count character frequencies using streams
    Map<Character, Long> charFreq = text.chars()
            .mapToObj(c -> (char) c)
            .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

    // Sort by frequency (descending)
    Map<Character, Long> sortedFreq = charFreq.entrySet().stream()
            .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (e1, e2) -> e1,
                LinkedHashMap::new
            ));

    // Build result string
    StringBuilder result = new StringBuilder("Character Distribution:\n");
    sortedFreq.forEach((character, count) -> {
        String displayChar = character == ' ' ? "SPACE" : character == '\n' ? "NEWLINE" : character.toString();
        result.append(displayChar).append(": ").append(count).append("\n");
    });

    resultTextArea.setText(result.toString());
    showStatus("Character distribution analysis completed.");
}
}