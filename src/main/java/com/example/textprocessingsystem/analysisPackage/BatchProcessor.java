package com.example.textprocessingsystem.analysisPackage;

import com.example.textprocessingsystem.collectionPackage.FileProcessor;
import com.example.textprocessingsystem.regex.RegexProcessor;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Service class for batch processing of multiple text files
 * with various operations.
 */
public class BatchProcessor {

    private final RegexProcessor regexProcessor;
    private final FileProcessor fileProcessor;
    private final int threadPoolSize;

    /**
     * Creates a BatchProcessor with default thread pool size.
     */
    public BatchProcessor() {
        this(Runtime.getRuntime().availableProcessors());
    }

    /**
     * Creates a BatchProcessor with specified thread pool size.
     *
     * @param threadPoolSize Number of threads to use for processing
     */
    public BatchProcessor(int threadPoolSize) {
        this.regexProcessor = new RegexProcessor();
        this.fileProcessor = new FileProcessor();
        this.threadPoolSize = threadPoolSize;
    }

    /**
     * Process multiple files with regex find and replace.
     *
     * @param inputFiles List of input files
     * @param outputDir Directory for output files
     * @param regex Regular expression pattern
     * @param replacement Replacement string
     * @param progressCallback Callback for progress updates
     * @return BatchResult containing operation statistics
     */
    public BatchResult batchFindReplace(List<File> inputFiles, File outputDir,
                                        String regex, String replacement,
                                        Consumer<BatchProgress> progressCallback) {
        Pattern pattern = Pattern.compile(regex);
        AtomicInteger processedFiles = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize)) {
            List<Future<?>> futures = new ArrayList<>();

            for (File inputFile : inputFiles) {
                futures.add(executorService.submit(() -> {
                    try {
                        String fileName = inputFile.getName();
                        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                        File outputFile = new File(outputDir, baseName + "_processed.txt");

                        // Process the file
                        fileProcessor.processFileByLine(inputFile, outputFile, line ->
                                pattern.matcher(line).replaceAll(replacement)
                        );

                        // Update progress
                        int completed = processedFiles.incrementAndGet();
                        if (progressCallback != null) {
                            progressCallback.accept(new BatchProgress(
                                    completed, inputFiles.size(), errorCount.get(), inputFile.getName()
                            ));
                        }

                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        if (progressCallback != null) {
                            progressCallback.accept(new BatchProgress(
                                    processedFiles.get(), inputFiles.size(), errorCount.get(),
                                    "Error processing " + inputFile.getName() + ": " + e.getMessage()
                            ));
                        }
                    }
                }));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }
        }

        return new BatchResult(
                processedFiles.get(),
                errorCount.get(),
                inputFiles.size(),
                "Regex Find and Replace"
        );
    }

    /**
     * Process files with regex extraction - only matching content.
     *
     * @param inputFiles List of input files
     * @param outputDir Directory for output files
     * @param regex Regular expression pattern
     * @param progressCallback Callback for progress updates
     * @return BatchResult containing operation statistics
     */
    public BatchResult batchExtract(List<File> inputFiles, File outputDir,
                                    String regex, Consumer<BatchProgress> progressCallback) {
        Pattern pattern = Pattern.compile(regex);
        AtomicInteger processedFiles = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try (ExecutorService executorService = Executors.newFixedThreadPool(threadPoolSize)) {
            List<Future<?>> futures = new ArrayList<>();

            for (File inputFile : inputFiles) {
                futures.add(executorService.submit(() -> {
                    try {
                        String fileName = inputFile.getName();
                        String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                        File outputFile = new File(outputDir, baseName + "_extracted.txt");

                        // Read the entire file
                        String content = fileProcessor.readFile(inputFile);
                        StringBuilder extractedContent = new StringBuilder();

                        // Extract matches
                        var matcher = pattern.matcher(content);
                        while (matcher.find()) {
                            extractedContent.append(matcher.group()).append(System.lineSeparator());
                        }

                        // Write extracted content
                        fileProcessor.writeFile(outputFile, extractedContent.toString());

                        // Update progress
                        int completed = processedFiles.incrementAndGet();
                        if (progressCallback != null) {
                            progressCallback.accept(new BatchProgress(
                                    completed, inputFiles.size(), errorCount.get(), inputFile.getName()
                            ));
                        }

                    } catch (Exception e) {
                        errorCount.incrementAndGet();
                        if (progressCallback != null) {
                            progressCallback.accept(new BatchProgress(
                                    processedFiles.get(), inputFiles.size(), errorCount.get(),
                                    "Error processing " + inputFile.getName() + ": " + e.getMessage()
                            ));
                        }
                    }
                }));
            }

            // Wait for all tasks to complete
            for (Future<?> future : futures) {
                try {
                    future.get();
                } catch (Exception e) {
                    errorCount.incrementAndGet();
                }
            }
        }

        return new BatchResult(
                processedFiles.get(),
                errorCount.get(),
                inputFiles.size(),
                "Regex Extract"
        );
    }

    /**
     * Merge multiple text files into a single output file.
     *
     * @param inputFiles List of input files
     * @param outputFile Output file
     * @param addSeparators Whether to add file separators between content
     * @param progressCallback Callback for progress updates
     * @return BatchResult containing operation statistics
     */
    public BatchResult mergeFiles(List<File> inputFiles, File outputFile,
                                  boolean addSeparators, Consumer<BatchProgress> progressCallback) {
        AtomicInteger processedFiles = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (File inputFile : inputFiles) {
                try {
                    // Add file separator if needed
                    if (addSeparators && processedFiles.get() > 0) {
                        writer.write("\n\n");
                        writer.write("=".repeat(50));
                        writer.write("\n");
                        writer.write("FILE: " + inputFile.getName());
                        writer.write("\n");
                        writer.write("=".repeat(50));
                        writer.write("\n\n");
                    }

                    // Read and write file content
                    List<String> lines = fileProcessor.readLines(inputFile);
                    for (String line : lines) {
                        writer.write(line);
                        writer.newLine();
                    }

                    // Update progress
                    int completed = processedFiles.incrementAndGet();
                    if (progressCallback != null) {
                        progressCallback.accept(new BatchProgress(
                                completed, inputFiles.size(), errorCount.get(), inputFile.getName()
                        ));
                    }

                } catch (Exception e) {
                    errorCount.incrementAndGet();
                    if (progressCallback != null) {
                        progressCallback.accept(new BatchProgress(
                                processedFiles.get(), inputFiles.size(), errorCount.get(),
                                "Error processing " + inputFile.getName() + ": " + e.getMessage()
                        ));
                    }
                }
            }
        } catch (IOException e) {
            errorCount.incrementAndGet();
            if (progressCallback != null) {
                progressCallback.accept(new BatchProgress(
                        processedFiles.get(), inputFiles.size(), errorCount.get(),
                        "Error writing to output file: " + e.getMessage()
                ));
            }
        }

        return new BatchResult(
                processedFiles.get(),
                errorCount.get(),
                inputFiles.size(),
                "File Merge"
        );
    }

    /**
     * Split a large file into multiple smaller files.
     *
     * @param inputFile Input file to split
     * @param outputDir Directory for output files
     * @param linesPerFile Maximum lines per output file
     * @param progressCallback Callback for progress updates
     * @return BatchResult containing operation statistics
     */
    public BatchResult splitFile(File inputFile, File outputDir,
                                 int linesPerFile, Consumer<BatchProgress> progressCallback) {
        AtomicInteger filesCreated = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);

        try {
            // Count total lines for progress reporting
            long totalLines = Files.lines(inputFile.toPath()).count();
            AtomicInteger processedLines = new AtomicInteger(0);

            // Read and split file
            try (BufferedReader reader = new BufferedReader(new FileReader(inputFile))) {
                List<String> currentFileLines = new ArrayList<>();
                String line;

                while ((line = reader.readLine()) != null) {
                    currentFileLines.add(line);
                    processedLines.incrementAndGet();

                    // When we reach the line limit, write to a file
                    if (currentFileLines.size() >= linesPerFile) {
                        writePartFile(currentFileLines, outputDir, inputFile.getName(),
                                filesCreated.incrementAndGet());
                        currentFileLines.clear();

                        // Report progress
                        if (progressCallback != null) {
                            progressCallback.accept(new BatchProgress(
                                    processedLines.get(), (int)totalLines, errorCount.get(),
                                    "Created part file " + filesCreated.get()
                            ));
                        }
                    }
                }

                // Write any remaining lines
                if (!currentFileLines.isEmpty()) {
                    writePartFile(currentFileLines, outputDir, inputFile.getName(),
                            filesCreated.incrementAndGet());

                    if (progressCallback != null) {
                        progressCallback.accept(new BatchProgress(
                                processedLines.get(), (int)totalLines, errorCount.get(),
                                "Created part file " + filesCreated.get()
                        ));
                    }
                }
            }

        } catch (IOException e) {
            errorCount.incrementAndGet();
            if (progressCallback != null) {
                progressCallback.accept(new BatchProgress(
                        0, 0, errorCount.get(),
                        "Error splitting file: " + e.getMessage()
                ));
            }
        }

        return new BatchResult(
                filesCreated.get(),
                errorCount.get(),
                1,
                "File Split"
        );
    }

    /**
     * Write a subset of lines to a part file.
     */
    private void writePartFile(List<String> lines, File outputDir,
                               String originalFileName, int partNumber) throws IOException {
        String baseName = originalFileName.substring(0, originalFileName.lastIndexOf('.'));
        File outputFile = new File(outputDir, baseName + "_part" + partNumber + ".txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Find files in a directory matching a pattern.
     *
     * @param directory Directory to search
     * @param filePattern Pattern for file names (glob syntax)
     * @return List of matching files
     */
    public List<File> findMatchingFiles(File directory, String filePattern) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        final Pattern pattern = Pattern.compile(filePattern.replace("*", ".*").replace("?", "."));

        return Files.walk(directory.toPath())
                .filter(Files::isRegularFile)
                .map(Path::toFile)
                .filter(file -> pattern.matcher(file.getName()).matches())
                .collect(Collectors.toList());
    }

    /**
     * Class representing batch operation results.
     */
    public static class BatchResult {
        private final int processedCount;
        private final int errorCount;
        private final int totalCount;
        private final String operationType;

        public BatchResult(int processedCount, int errorCount, int totalCount, String operationType) {
            this.processedCount = processedCount;
            this.errorCount = errorCount;
            this.totalCount = totalCount;
            this.operationType = operationType;
        }

        public int getProcessedCount() {
            return processedCount;
        }

        public int getErrorCount() {
            return errorCount;
        }

        public int getTotalCount() {
            return totalCount;
        }

        public String getOperationType() {
            return operationType;
        }

        public int getSuccessCount() {
            return processedCount - errorCount;
        }

        @Override
        public String toString() {
            return String.format("%s completed: %d/%d files processed, %d errors",
                    operationType, processedCount, totalCount, errorCount);
        }
    }

    /**
     * Class representing batch operation progress.
     */
    public static class BatchProgress {
        private final int completed;
        private final int total;
        private final int errors;
        private final String message;

        public BatchProgress(int completed, int total, int errors, String message) {
            this.completed = completed;
            this.total = total;
            this.errors = errors;
            this.message = message;
        }

        public int getCompleted() {
            return completed;
        }

        public int getTotal() {
            return total;
        }

        public int getErrors() {
            return errors;
        }

        public String getMessage() {
            return message;
        }
    }
}

