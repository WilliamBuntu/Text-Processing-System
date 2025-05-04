package com.example.textprocessingsystem.regex;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.regex.PatternSyntaxException;

import static org.junit.jupiter.api.Assertions.*;

class RegexProcessorTest {

    private RegexProcessor regexProcessor;

    @BeforeEach
    void setUp() {
        regexProcessor = new RegexProcessor();
    }

   @Test
   void findMatches() {
       String text = "Email me at test@example.com or contact admin@example.org";
       String regex = "\\w+@\\w+\\.\\w+";

       List<RegexProcessor.Match> matches = regexProcessor.findMatches(text, regex);

       // Verify the correct number of matches
       assertEquals(2, matches.size());

       // Verify the content of matches
       assertEquals("test@example.com", matches.get(0).getContent());
       assertEquals("admin@example.org", matches.get(1).getContent());

         // Verify the start and end indices of matches
       int expectedStartIndex1 = text.indexOf("test@example.com");
       int expectedEndIndex1 = expectedStartIndex1 + "test@example.com".length();
       assertEquals(expectedStartIndex1, matches.getFirst().getStart());
       assertEquals(expectedEndIndex1, matches.getFirst().getEnd());

       int expectedStartIndex2 = text.indexOf("admin@example.org");
       int expectedEndIndex2 = expectedStartIndex2 + "admin@example.org".length();
       assertEquals(expectedStartIndex2, matches.get(1).getStart());
       assertEquals(expectedEndIndex2, matches.get(1).getEnd());
   }

    @Test
    void findMatchesWithGroups() {
        String text = "Order #12345 was placed on 2023-05-15";
        String regex = "Order #(\\d+) was placed on (\\d{4}-\\d{2}-\\d{2})";

        List<RegexProcessor.Match> matches = regexProcessor.findMatches(text, regex);

        assertEquals(1, matches.size());
        assertEquals("Order #12345 was placed on 2023-05-15", matches.get(0).getContent());
        assertEquals("12345", matches.get(0).getGroups()[0]);
        assertEquals("2023-05-15", matches.get(0).getGroups()[1]);
    }

    @Test
    void findMatchesNoMatches() {
        String text = "Nothing to match here!";
        String regex = "\\d+";

        List<RegexProcessor.Match> matches = regexProcessor.findMatches(text, regex);

        assertTrue(matches.isEmpty());
    }

    @Test
    void findMatchesInvalidRegex() {
        String text = "Sample text";
        String regex = "([unclosed group";

        assertThrows(PatternSyntaxException.class, () -> {
            regexProcessor.findMatches(text, regex);
        });
    }

    @Test
    void replaceAll() {
        String text = "Replace all 123 numbers 456 with X";
        String regex = "\\d+";
        String replacement = "X";

        String result = regexProcessor.replaceAll(text, regex, replacement);

        assertEquals("Replace all X numbers X with X", result);
    }

    @Test
    void replaceAllWithBackReferences() {
        String text = "John Smith, Jane Doe";
        String regex = "(\\w+)\\s+(\\w+)";
        String replacement = "$2, $1";

        String result = regexProcessor.replaceAll(text, regex, replacement);

        assertEquals("Smith, John, Doe, Jane", result);
    }

    @Test
    void replaceAllNoMatches() {
        String text = "No numbers here";
        String regex = "\\d+";
        String replacement = "X";

        String result = regexProcessor.replaceAll(text, regex, replacement);

        assertEquals(text, result);
    }

    @Test
    void replaceFirst() {
        String text = "Replace first 123 but not 456";
        String regex = "\\d+";
        String replacement = "X";

        String result = regexProcessor.replaceFirst(text, regex, replacement);

        assertEquals("Replace first X but not 456", result);
    }

    @Test
    void replaceFirstWithBackReferences() {
        String text = "John Smith and Jane Doe";
        String regex = "(\\w+)\\s+(\\w+)";
        String replacement = "$2, $1";

        String result = regexProcessor.replaceFirst(text, regex, replacement);

        assertEquals("Smith, John and Jane Doe", result);
    }

    @Test
    void replaceFirstNoMatches() {
        String text = "No numbers here";
        String regex = "\\d+";
        String replacement = "X";

        String result = regexProcessor.replaceFirst(text, regex, replacement);

        assertEquals(text, result);
    }

    @Test
    void split() {
        String text = "apple,banana,orange,grape";
        String regex = ",";

        String[] parts = regexProcessor.split(text, regex);

        assertEquals(4, parts.length);
        assertEquals("apple", parts[0]);
        assertEquals("banana", parts[1]);
        assertEquals("orange", parts[2]);
        assertEquals("grape", parts[3]);
    }

    @Test
    void splitWithLimit() {
        String text = "apple,banana,orange,grape";
        String regex = ",";

        // Using Java's built-in split with limit
        String[] parts = text.split(regex, 2);

        assertEquals(2, parts.length);
        assertEquals("apple", parts[0]);
        assertEquals("banana,orange,grape", parts[1]);
    }

    @Test
    void splitComplex() {
        String text = "apple, banana; orange: grape";
        String regex = "[,;:]\\s*";

        String[] parts = regexProcessor.split(text, regex);

        assertEquals(4, parts.length);
        assertEquals("apple", parts[0]);
        assertEquals("banana", parts[1]);
        assertEquals("orange", parts[2]);
        assertEquals("grape", parts[3]);
    }

    @Test
    void matches() {
        String text = "12345";
        String regex = "\\d+";

        boolean result = regexProcessor.matches(text, regex);

        assertTrue(result);
    }

    @Test
    void matchesPartial() {
        String text = "12345abc";
        String regex = "\\d+";

        boolean result = regexProcessor.matches(text, regex);

        assertFalse(result);
    }

    @Test
    void matchesComplex() {
        String email = "user.name@example.com";
        String regex = "[\\w.]+@[\\w.]+\\.[a-z]{2,}";

        boolean result = regexProcessor.matches(email, regex);

        assertTrue(result);
    }

    @Test
    void isValidRegex() {
        String regex = "\\d+";

        boolean isValid = regexProcessor.isValidRegex(regex);

        assertTrue(isValid);
    }

    @Test
    void isValidRegexNegative() {
        String regex = "[unclosed bracket";

        boolean isValid = regexProcessor.isValidRegex(regex);

        assertFalse(isValid);
    }

    @Test
    void isValidRegexEdgeCases() {
        assertTrue(regexProcessor.isValidRegex(""));  // Empty regex is valid
        assertTrue(regexProcessor.isValidRegex(".*")); // Simple wildcard
        assertFalse(regexProcessor.isValidRegex("\\"));  // Unfinished escape character
    }

    @Test
    void getRegexError() {
        String invalidRegex = "[unclosed bracket";

        String errorMessage = regexProcessor.getRegexError(invalidRegex);

        assertNotNull(errorMessage);
        assertTrue(errorMessage.contains("Unclosed character class"));
    }

    @Test
    void getRegexErrorValidPattern() {
        String validRegex = "\\d+";

        String errorMessage = regexProcessor.getRegexError(validRegex);

        assertNull(errorMessage);
    }
}