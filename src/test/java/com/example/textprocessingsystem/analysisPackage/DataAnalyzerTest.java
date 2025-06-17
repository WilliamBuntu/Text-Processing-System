package com.example.textprocessingsystem.analysisPackage;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class DataAnalyzerTest {


    private final DataAnalyzer dataAnalyzer = new DataAnalyzer();

    @Test
    void analyzeWordFrequency() {
        String text = "hello world hello";
        Map<String, Long> result = dataAnalyzer.analyzeWordFrequency(text);

        assertEquals(2, result.get("hello"));
        assertEquals(1, result.get("world"));
        assertEquals(2, result.size());
    }

    @Test
    void analyzeCharacterDistribution() {
        String text = "hello";
        Map<Character, Long> result = dataAnalyzer.analyzeCharacterDistribution(text);

        assertEquals(1, result.get('h'));
        assertEquals(2, result.get('l'));
        assertEquals(1, result.get('o'));
        assertEquals(4, result.size());
    }


    @Test
    void analyzeLineLength() {
    String text = "line one\nline two\nline three";
    DataAnalyzer.LineStatistics stats = dataAnalyzer.analyzeLineLength(text);

    assertEquals(3, stats.lineCount());
    assertEquals(8.67, stats.averageLength(), 0.01);
    assertEquals(8, stats.minLength());
    assertEquals(10, stats.maxLength());
    }
    @Test
    void analyzePatternOccurrence() {
        String text = "test@example.com test@example.com another@test.com";
        String regex = "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b";
        DataAnalyzer.PatternStatistics stats = dataAnalyzer.analyzePatternOccurrence(text, regex);

        assertEquals(3, stats.getTotalOccurrences());
        assertEquals(2, stats.getUniqueOccurrences());
        assertTrue(stats.getExamples().contains("test@example.com"));
        assertTrue(stats.getExamples().contains("another@test.com"));
    }

    @Test
    void analyzeCommonPatterns() {
        String text = "Contact me at test@example.com or visit https://example.com";
        Map<String, DataAnalyzer.PatternStatistics> result = dataAnalyzer.analyzeCommonPatterns(text);

        assertTrue(result.containsKey("Email addresses"));
        assertTrue(result.containsKey("URLs"));

        DataAnalyzer.PatternStatistics emailStats = result.get("Email addresses");
        assertEquals(1, emailStats.getTotalOccurrences());
        assertTrue(emailStats.getExamples().contains("test@example.com"));

        DataAnalyzer.PatternStatistics urlStats = result.get("URLs");
        assertEquals(1, urlStats.getTotalOccurrences());
//        assertTrue(urlStats.getExamples().contains("https://example.com"));
    }
}