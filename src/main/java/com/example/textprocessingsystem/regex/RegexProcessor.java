package com.example.textprocessingsystem.regex;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Service class that handles regular expression operations
 * for the DataFlow Text Processor application.
 */
public class RegexProcessor {

    /**
     * Find all matches of a regex pattern in the given text.
     *
     * @param text The text to search in
     * @param regex The regular expression pattern
     * @return A list of Match objects containing position and content information
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public List<Match> findMatches(String text, String regex) throws PatternSyntaxException {
        List<Match> matches = new ArrayList<>();

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        while (matcher.find()) {
            matches.add(new Match(
                    matcher.start(),
                    matcher.end(),
                    matcher.group(),
                    extractGroups(matcher)
            ));
        }

        return matches;
    }

    /**
     * Replace all occurrences of a regex pattern in the given text.
     *
     * @param text The text to perform replacements on
     * @param regex The regular expression pattern
     * @param replacement The replacement string
     * @return The text with all matches replaced
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public String replaceAll(String text, String regex, String replacement) throws PatternSyntaxException {

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceAll(replacement);

    }

    /**
     * Replace the first occurrence of a regex pattern in the given text.
     *
     * @param text The text to perform replacement on
     * @param regex The regular expression pattern
     * @param replacement The replacement string
     * @return The text with the first match replaced
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public String replaceFirst(String text, String regex, String replacement) throws PatternSyntaxException {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);
        return matcher.replaceFirst(replacement);
    }

    /**
     * Split text by a regex pattern.
     *
     * @param text The text to split
     * @param regex The regular expression pattern
     * @return An array of strings
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public String[] split(String text, String regex) throws PatternSyntaxException {
        return text.split(regex);
    }

    /**
     * Check if the text matches the pattern completely.
     *
     * @param text The text to check
     * @param regex The regular expression pattern
     * @return true if the entire text matches the pattern
     * @throws PatternSyntaxException If the regex pattern is invalid
     */
    public boolean matches(String text, String regex) throws PatternSyntaxException {
        return Pattern.matches(regex, text);
    }

    /**
     * Extract capture groups from a matcher.
     *
     * @param matcher The matcher with found results
     * @return An array of strings with captured groups
     */
    private String[] extractGroups(Matcher matcher) {
        int groupCount = matcher.groupCount();
        String[] groups = new String[groupCount];

        for (int i = 0; i < groupCount; i++) {
            groups[i] = matcher.group(i + 1);
        }

        return groups;
    }

    /**
     * Validate if a regex pattern is syntactically correct.
     *
     * @param regex The pattern to validate
     * @return true if valid, false if invalid
     */
    public boolean isValidRegex(String regex) {
        try {
            Pattern.compile(regex);
            return true;
        } catch (PatternSyntaxException e) {
            return false;
        }
    }

    /**
     * Get error details for an invalid regex pattern.
     *
     * @param regex The invalid pattern
     * @return Error message or null if the pattern is valid
     */
    public String getRegexError(String regex) {
        try {
            Pattern.compile(regex);
            return null;
        } catch (PatternSyntaxException e) {
            return e.getMessage();
        }
    }

    /**
     * Class representing a regex match with position and content information.
     */
    public static class Match {
        private final int start;
        private final int end;
        private final String content;
        private final String[] groups;

        public Match(int start, int end, String content, String[] groups) {
            this.start = start;
            this.end = end;
            this.content = content;
            this.groups = groups;
        }

        public int getStart() {
            return start;
        }

        public int getEnd() {
            return end;
        }

        public String getContent() {
            return content;
        }

        public String[] getGroups() {
            return groups;
        }

        @Override
        public String toString() {
            return String.format("Match[%d-%d]: %s", start, end, content);
        }
    }
}