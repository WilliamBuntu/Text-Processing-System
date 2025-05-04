package com.example.textprocessingsystem.analysisPackage;
import java.util.*;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class that provides text data analysis functionality
 * using Java Streams API for efficient processing.
 */
public class DataAnalyzer {

    /**
     * Analyze word frequency in the given text.
     *
     * @param text The text to analyze
     * @return Map of words to their frequency, sorted by frequency (descending)
     */
    public Map<String, Long> analyzeWordFrequency(String text) {
        return Arrays.stream(text.split("\\s+"))
                .filter(word -> !word.isEmpty())
                .map(word -> word.replaceAll("[^a-zA-Z0-9]", "").toLowerCase())
                .filter(word -> !word.isEmpty())
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    /**
     * Analyze character distribution in the given text.
     *
     * @param text The text to analyze
     * @return Map of characters to their frequency, sorted by frequency (descending)
     */
    public Map<Character, Long> analyzeCharacterDistribution(String text) {
        return text.chars()
                .mapToObj(c -> (char) c)
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<Character, Long>comparingByValue().reversed())
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));
    }

    /**
     * Calculate statistics for line lengths in the given text.
     *
     * @param text The text to analyze
     * @return A LineStatistics object containing the statistics
     */
    public LineStatistics analyzeLineLength(String text) {
        String[] lines = text.split("\n");

        // Using DoubleSummaryStatistics to compute statistics in one pass
        DoubleSummaryStatistics stats = Arrays.stream(lines)
                .mapToDouble(String::length)
                .summaryStatistics();

        // Calculate the distribution of line lengths
        int[] distribution = new int[10]; // 0-9, 10-19, ..., 90+
        Arrays.stream(lines).forEach(line -> {
            int length = line.length();
            int index = Math.min(length / 10, 9);
            distribution[index]++;
        });

        return new LineStatistics(
                lines.length,
                stats.getAverage(),
                (int) stats.getMin(),
                (int) stats.getMax(),
                distribution
        );
    }

    /**
     * Find occurrences of a pattern in the given text.
     *
     * @param text The text to analyze
     * @param regex The regular expression pattern
     * @return A PatternStatistics object containing the statistics
     */
    public PatternStatistics analyzePatternOccurrence(String text, String regex) {
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(text);

        List<String> matches = new ArrayList<>();
        int count = 0;

        while (matcher.find()) {
            count++;
            matches.add(matcher.group());
        }

        Set<String> uniqueMatches = new HashSet<>(matches);

        // Get frequencies of unique matches
        Map<String, Long> frequencies = matches.stream()
                .collect(Collectors.groupingBy(
                        Function.identity(),
                        Collectors.counting()))
                .entrySet().stream()
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
                .limit(10) // Get top 10 most frequent
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new));

        return new PatternStatistics(
                count,
                uniqueMatches.size(),
                uniqueMatches.stream().limit(10).collect(Collectors.toList()),
                frequencies
        );
    }

    /**
     * Analyze common patterns in the text.
     *
     * @param text The text to analyze
     * @return Map of pattern names to their statistics
     */
    public Map<String, PatternStatistics> analyzeCommonPatterns(String text) {
        Map<String, String> patterns = new HashMap<>();
        patterns.put("Email addresses", "\\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}\\b");
        patterns.put("Phone numbers", "\\b\\d{3}[-.]?\\d{3}[-.]?\\d{4}\\b");
        patterns.put("URLs", "https?://\\S+|www\\.\\S+");
        patterns.put("Numeric values", "\\b\\d+\\b");
        patterns.put("Capitalized words", "\\b[A-Z][a-z]+\\b");
        patterns.put("Hashtags", "#\\w+");

        return patterns.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> analyzePatternOccurrence(text, entry.getValue())
                ));
    }

    /**
     * Class representing statistics for line lengths.
     */
    public static class LineStatistics {
        private final int lineCount;
        private final double averageLength;
        private final int minLength;
        private final int maxLength;
        private final int[] distribution;

        public LineStatistics(int lineCount, double averageLength, int minLength, int maxLength, int[] distribution) {
            this.lineCount = lineCount;
            this.averageLength = averageLength;
            this.minLength = minLength;
            this.maxLength = maxLength;
            this.distribution = distribution;
        }

        public int getLineCount() {
            return lineCount;
        }

        public double getAverageLength() {
            return averageLength;
        }

        public int getMinLength() {
            return minLength;
        }

        public int getMaxLength() {
            return maxLength;
        }

        public int[] getDistribution() {
            return distribution;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("Line Statistics:\n");
            sb.append(String.format("Total Lines: %d\n", lineCount));
            sb.append(String.format("Average Length: %.2f characters\n", averageLength));
            sb.append(String.format("Minimum Length: %d characters\n", minLength));
            sb.append(String.format("Maximum Length: %d characters\n", maxLength));

            sb.append("\nLength Distribution:\n");
            for (int i = 0; i < 9; i++) {
                sb.append(String.format("%d-%d chars: %d lines\n",
                        i * 10, (i + 1) * 10 - 1, distribution[i]));
            }
            sb.append(String.format("90+ chars: %d lines\n", distribution[9]));

            return sb.toString();
        }
    }

    /**
     * Class representing statistics for pattern occurrences.
     */
    public static class PatternStatistics {
        private final int totalOccurrences;
        private final int uniqueOccurrences;
        private final List<String> examples;
        private final Map<String, Long> topFrequencies;

        public PatternStatistics(int totalOccurrences, int uniqueOccurrences,
                                 List<String> examples, Map<String, Long> topFrequencies) {
            this.totalOccurrences = totalOccurrences;
            this.uniqueOccurrences = uniqueOccurrences;
            this.examples = examples;
            this.topFrequencies = topFrequencies;
        }

        public int getTotalOccurrences() {
            return totalOccurrences;
        }

        public int getUniqueOccurrences() {
            return uniqueOccurrences;
        }

        public List<String> getExamples() {
            return examples;
        }

        public Map<String, Long> getTopFrequencies() {
            return topFrequencies;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("Total occurrences: %d\n", totalOccurrences));
            sb.append(String.format("Unique occurrences: %d\n", uniqueOccurrences));

            if (!examples.isEmpty()) {
                sb.append("Examples: ");
                sb.append(String.join(", ", examples.stream().limit(5).collect(Collectors.toList())));
                sb.append("\n");
            }

            if (!topFrequencies.isEmpty()) {
                sb.append("\nMost frequent:\n");
                topFrequencies.forEach((match, count) ->
                        sb.append(String.format("%s: %d occurrences\n", match, count))
                );
            }

            return sb.toString();
        }
    }
}