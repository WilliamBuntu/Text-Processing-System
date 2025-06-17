package com.example.textprocessingsystem.utils;

import okhttp3.*;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.IOException;
import java.util.logging.Logger;

/**
 * Summarizer that uses the Hugging Face API with the facebook/bart-large-cnn model
 * to generate summaries similar to the Python transformers pipeline example.
 */
public class BartCnnSummarizer {
    private static final Logger logger = Logger.getLogger(BartCnnSummarizer.class.getName());
    private static final String API_URL = "https://api-inference.huggingface.co/models/facebook/bart-large-cnn";
    private final String apiKey;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
            .build();

    /**
     * Create a new summarizer
     * @param apiKey Your Hugging Face API key (get one for free at https://huggingface.co/settings/tokens)
     */
    public BartCnnSummarizer(String apiKey) {
        this.apiKey = apiKey;
    }

    /**
     * Generate a summary of the given text using the facebook/bart-large-cnn model
     * @param text Text to summarize
     * @param maxLength Maximum length of summary (in tokens)
     * @param minLength Minimum length of summary (in tokens)
     * @return The generated summary
     */
    public String summarize(String text, int maxLength, int minLength) {
        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }

        try {
            // Create JSON payload similar to the Python example
            JSONObject payload = new JSONObject();
            payload.put("inputs", text);
            
            // Set parameters matching the Python example
            JSONObject parameters = new JSONObject();
            parameters.put("max_length", maxLength);
            parameters.put("min_length", minLength);
            parameters.put("do_sample", false);
            payload.put("parameters", parameters);
            
            // Build HTTP request
            RequestBody body = RequestBody.create(
                payload.toString(), 
                MediaType.parse("application/json")
            );
            
            Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Authorization", "Bearer " + apiKey)
                .build();
            
            // Execute request
            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful()) {
                    logger.warning("API returned error: " + response.code() + " - " + response.message());
                    return fallbackSummarize(text);
                }
                
                String responseBody = response.body().string();
                
                // Parse response based on format
                if (responseBody.startsWith("[")) {
                    // Response is an array format
                    JSONArray jsonArray = new JSONArray(responseBody);
                    JSONObject firstResult = jsonArray.getJSONObject(0);
                    return firstResult.getString("summary_text");
                } else {
                    // Response is a single object format
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    if (jsonResponse.has("summary_text")) {
                        return jsonResponse.getString("summary_text");
                    } else {
                        logger.warning("Unexpected response format: " + responseBody);
                        return fallbackSummarize(text);
                    }
                }
            }
        } catch (Exception e) {
            logger.severe("Error generating summary: " + e.getMessage());
            e.printStackTrace();
            return fallbackSummarize(text);
        }
    }
    
    /**
     * Overloaded method with default parameters
     */
    public String summarize(String text) {
        return summarize(text, 130, 30);
    }
    
    /**
     * Basic fallback summarization method when API fails
     */
    private String fallbackSummarize(String text) {
        String[] sentences = text.split("[.!?]\\s+");
        int numSentences = Math.min(3, sentences.length);
        
        StringBuilder summary = new StringBuilder("Summary (fallback):\n");
        for (int i = 0; i < numSentences; i++) {
            summary.append(sentences[i].trim()).append(".\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Example implementation to replace the OpenAI version in your JavaFX application
     */
    public static String generateSummary(String text) {
        if (text == null || text.trim().isEmpty()) {
            return "No content to summarize.";
        }

        try {
            // Get API key from environment variable (you can also hardcode for testing)
            String apiKey = System.getenv("HUGGINGFACE_API_KEY");
            if (apiKey == null || apiKey.isEmpty()) {
                throw new RuntimeException("HUGGINGFACE_API_KEY environment variable not set");
            }
            
            // Create summarizer and generate summary
            BartCnnSummarizer summarizer = new BartCnnSummarizer(apiKey);
            String summary = summarizer.summarize(text);
            
            return "Summary:\n" + summary.trim();
        } catch (Exception e) {
            Logger.getLogger("BartCnnSummarizer").severe("Error generating summary: " + e.getMessage());
            e.printStackTrace();
            
            // Fallback to extractive summary
            return generateFallbackSummary(text);
        }
    }
    
    /**
     * Example fallback method to use in the generateSummary method
     */
    private static String generateFallbackSummary(String text) {
        String[] sentences = text.split("[.!?]\\s+");
        int summaryLength = Math.min(3, sentences.length);
        
        StringBuilder summary = new StringBuilder("Summary (basic fallback):\n");
        for (int i = 0; i < summaryLength; i++) {
            summary.append(sentences[i].trim()).append(".\n");
        }
        
        return summary.toString();
    }
    
    /**
     * Example usage similar to the Python example
     */
    public static void main(String[] args) {
        // Example text matching the Python example
        String article = "New York (CNN)When Liana Barrientos was 23 years old, she got married in Westchester County, New York.\n" +
                "A year later, she got married again in Westchester County, but to a different man and without divorcing her first husband.\n" +
                "Only 18 days after that marriage, she got hitched yet again. Then, Barrientos declared \"I do\" five more times, sometimes only within two weeks of each other.\n" +
                "In 2010, she married once more, this time in the Bronx. In an application for a marriage license, she stated it was her \"first and only\" marriage.\n" +
                "Barrientos, now 39, is facing two criminal counts of \"offering a false instrument for filing in the first degree,\" referring to her false statements on the\n" +
                "2010 marriage license application, according to court documents.\n" +
                "Prosecutors said the marriages were part of an immigration scam.\n" +
                "On Friday, she pleaded not guilty at State Supreme Court in the Bronx, according to her attorney, Christopher Wright, who declined to comment further.\n" +
                "After leaving court, Barrientos was arrested and charged with theft of service and criminal trespass for allegedly sneaking into the New York subway through an emergency exit, said Detective\n" +
                "Annette Markowski, a police spokeswoman. In total, Barrientos has been married 10 times, with nine of her marriages occurring between 1999";
        
        // Create summarizer with your API key
        BartCnnSummarizer summarizer = new BartCnnSummarizer(System.getenv("HUGGINGFACE_API_KEY"));
        
        // Generate summary with same parameters as Python example
        String summary = summarizer.summarize(article, 130, 30);
        System.out.println("Summary: " + summary);
    }
}