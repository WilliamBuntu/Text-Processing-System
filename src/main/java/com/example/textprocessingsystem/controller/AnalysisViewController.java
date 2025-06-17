package com.example.textprocessingsystem.controller;

import com.example.textprocessingsystem.analysisPackage.DataAnalyzer;
import com.example.textprocessingsystem.utils.BartCnnSummarizer;
import com.example.textprocessingsystem.utils.CustomLogger;
import com.example.textprocessingsystem.utils.ErrorHandler;
import com.example.textprocessingsystem.utils.GlobalAlert;
import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatMessage;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import org.jetbrains.annotations.NotNull;

import com.theokanning.openai.completion.chat.ChatCompletionRequest;
import com.theokanning.openai.completion.chat.ChatCompletionResult;
import com.theokanning.openai.completion.chat.ChatMessage;
import com.theokanning.openai.service.OpenAiService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;


public class AnalysisViewController {

    private static final Logger logger = CustomLogger.createLogger(RegexPanelController.class.getName());


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
    @FXML
    private Button analyzeLineLengthButton;
    @FXML
    private Button analyzeCommonPatternsButton;

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
            logger.warning("Input text is empty.");
            GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text for analysis.");
            showStatus("Input text is required for word frequency analysis.");
            return;
        }
        try {
            DataAnalyzer dataAnalyzer = new DataAnalyzer();
            Map<String, Long> wordFrequencies = dataAnalyzer.analyzeWordFrequency(text);
            StringBuilder result = new StringBuilder("Word Frequencies:\n");
            wordFrequencies.forEach((word, count) -> result.append(word).append(": ").append(count).append("\n"));
            resultTextArea.setText(result.toString());

        } catch (IllegalArgumentException e) {
            logger.log(java.util.logging.Level.SEVERE, "Invalid input: " + e.getMessage(), e);
            ErrorHandler.handleException("Invalid input", e);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "An unexpected error occurred during analysis: " + e.getMessage(), e);
            ErrorHandler.handleException("An unexpected error occurred during analysis.", e);
        }
        logger.info("Word frequency analysis completed.");
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
            logger.warning("Input text or pattern is empty.");
            GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text and pattern for analysis.");
            showStatus("Input text and pattern are required for pattern frequency analysis.");
            return;
        }

        String result = analyzePatternFrequencies(text, pattern);
        resultTextArea.setText(result);
        logger.info("Pattern frequency analysis completed.");
        showStatus("Pattern frequency analysis completed.");
    }

    /**
     * Handles the action for generating a text summary.
     */
    @FXML
    private void handleGenerateSummary() {

        String text = inputTextArea.getText();
        if (text.isEmpty()) {
            logger.warning("Input text is empty.");
            GlobalAlert.showAlert( Alert.AlertType.WARNING, "Input Required", "Please enter text for summary generation.");
            showStatus("Input text is required for generating a summary.");
            return;
        }

        String result = generateSummary(text);
        resultTextArea.setText(result);
        logger.info("Text summary generated.");
        showStatus("Text summary generated.");
    }



    /**
     * Analyzes pattern frequencies in the given text.
     *
     * @param text The input text
     * @param pattern The regex pattern
     * @return A formatted string of pattern frequencies
     */
    private String analyzePatternFrequencies(String text, String pattern) {
        if (text.isEmpty() || pattern.isEmpty()) {
            logger.warning("Input text or pattern is empty.");
            GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text and pattern for analysis.");
            showStatus("Input text and pattern are required for pattern frequency analysis.");
            return "";
        }
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
    /**
     * Generates a summary of the provided text using Hugging Face's BART-CNN model
     * This method replaces the previous OpenAI-based implementation
     */
    private String generateSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }

        try {
            // Get API key from environment variable
            String apiKey = "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";
            if (apiKey == null || apiKey.isEmpty()) {
                // For development, you could hardcode a key here or show a dialog to get it
                // apiKey = "your_api_key_here"; // For testing only
                throw new RuntimeException("HUGGINGFACE_API_KEY environment variable not set");
            }

            // Create summarizer and generate summary
            BartCnnSummarizer summarizer = new BartCnnSummarizer(apiKey);
            String summary = summarizer.summarize(text, 150, 30);

            return "Summary:\n" + summary.trim();
        } catch (Exception e) {
            logger.severe("Error generating AI summary: " + e.getMessage());
            e.printStackTrace();

            // Fall back to basic summary if AI fails
            return generateFallbackSummary(text);
        }
    }

    /**
     * Generates a basic extractive summary when the AI summarization fails
     */
    private String generateFallbackSummary(String text) {
        String[] sentences = text.split("[.!?]\\s+");
        int summaryLength = Math.min(3, sentences.length);

        StringBuilder summary = new StringBuilder("Summary (basic fallback):\n");
        for (int i = 0; i < summaryLength; i++) {
            summary.append(sentences[i].trim()).append(".\n");
        }

        return summary.toString();
    }

    /**
     * Example method that would be connected to your UI button
     */
//    public void handleGenerateSummary() {
//        try {
//            // Get text from your UI component
//            String text = inputTextArea.getText(); // Replace with your actual text source
//
//            // Generate and display summary
//            String summary = generateSummary(text);
//
//            // Update UI with the summary
//            resultTextArea.setText(summary); // Replace with your actual UI component
//        } catch (Exception e) {
//            logger.severe("Error handling summary generation: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }

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
     * @param actionEvent The input text
     * @return A formatted string of character distribution
     */
    @FXML
    public void analyzeCharacterDistribution(ActionEvent actionEvent) {
         try {

            String text = inputTextArea.getText();
            if (text.isEmpty()) {
                GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text for analysis.");

                showStatus("Input text is required for character distribution analysis.");
                return;
            }
            // Use DataAnalyzer to analyze character distribution
             final var result = getStringBuilder(text);

            resultTextArea.setText(result.toString());
            logger.info("Character distribution analysis completed.");
            showStatus("Character distribution analysis completed.");
        }catch (IllegalArgumentException e) {
            logger.log(java.util.logging.Level.SEVERE, "Invalid input: " + e.getMessage(), e);
            ErrorHandler.handleException("Invalid input", e);
        }catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "An unexpected error occurred during analysis: " + e.getMessage(), e);
            ErrorHandler.handleException("An unexpected error occurred during analysis.", e);
        }

    }

    @NotNull
    private static StringBuilder getStringBuilder(String text) {
        DataAnalyzer dataAnalyzer = new DataAnalyzer();
        Map<Character, Long> charFrequencies = dataAnalyzer.analyzeCharacterDistribution(text);
        // Format the result

        StringBuilder result = new StringBuilder("Character Distribution:\n");
        charFrequencies.forEach((character, count) -> {
            String displayChar = character == ' ' ? "Space" : character == '\n' ? "Newline" : character.toString();
            result.append(displayChar).append(": ").append(count).append("\n");
        });
        return result;
    }

    @FXML
    public void analyzeLineLength(ActionEvent actionEvent) {
        try {
            String text = inputTextArea.getText();
            if (text.isEmpty()) {
                logger.warning("Input text is empty.");
                GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text for analysis.");
                showStatus("Input text is required for line length analysis.");
                return;
            }

            // Use DataAnalyzer to analyze line lengths
            DataAnalyzer dataAnalyzer = new DataAnalyzer();
            DataAnalyzer.LineStatistics lineStats = dataAnalyzer.analyzeLineLength(text);

            // The LineStatistics class already has a toString() method that formats the results nicely
            resultTextArea.setText(lineStats.toString());
            logger.info("Line length analysis completed.");
            showStatus("Line length analysis completed.");
        } catch (IllegalArgumentException e) {
            logger.log(java.util.logging.Level.SEVERE, "Invalid input: " + e.getMessage(), e);
            ErrorHandler.handleException("Invalid input", e);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "An unexpected error occurred during analysis: " + e.getMessage(), e);
            ErrorHandler.handleException("An unexpected error occurred during analysis.", e);
        }
    }



    /**
     * Analyzes common patterns in the given text.
     *
     * @param actionEvent The input text
     * @return A formatted string of common patterns
     */
    @FXML
    public void analyzeCommonPatterns(ActionEvent actionEvent) {
        try {
            String text = inputTextArea.getText();
            if (text.isEmpty()) {
                logger.warning("Input text is empty.");
                GlobalAlert.showAlert(Alert.AlertType.WARNING, "Input Required", "Please enter text for analysis.");
                showStatus("Input text is required for common patterns analysis.");
                return;
            }

            // Use DataAnalyzer to analyze common patterns
            DataAnalyzer dataAnalyzer = new DataAnalyzer();
            Map<String, DataAnalyzer.PatternStatistics> patternStats = dataAnalyzer.analyzeCommonPatterns(text);

            // Format the results
            StringBuilder result = new StringBuilder("Common Patterns Analysis:\n\n");
            patternStats.forEach((patternName, stats) -> {
                result.append("=== ").append(patternName).append(" ===\n");
                result.append(stats.toString()).append("\n");
            });

            resultTextArea.setText(result.toString());
            logger.info("Common patterns analysis completed.");
            showStatus("Common patterns analysis completed.");
        } catch (IllegalArgumentException e) {
            logger.log(java.util.logging.Level.SEVERE, "Invalid input: " + e.getMessage(), e);
            ErrorHandler.handleException("Invalid input", e);
        } catch (Exception e) {
            logger.log(java.util.logging.Level.SEVERE, "An unexpected error occurred during analysis: " + e.getMessage(), e);
            ErrorHandler.handleException("An unexpected error occurred during analysis.", e);
        }
    }

}