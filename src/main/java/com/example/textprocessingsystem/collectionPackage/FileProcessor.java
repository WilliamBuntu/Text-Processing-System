package com.example.textprocessingsystem.collectionPackage;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Service class that handles file operations for the
 * DataFlow Text Processor application.
 */
public class FileProcessor {

    /**
     * Read the contents of a file as a string.
     *
     * @param file The file to read
     * @return The file contents as a string
     * @throws IOException If there is an error reading the file
     */
    public String readFile(File file) throws IOException {
        return Files.readString(file.toPath(), StandardCharsets.UTF_8);
    }

    /**
     * Read a file line by line using BufferedReader for efficient
     * large file processing.
     *
     * @param file The file to read
     * @return A list of lines from the file
     * @throws IOException If there is an error reading the file
     */
    public List<String> readLines(File file) throws IOException {
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }

        return lines;
    }

    /**
     * Read a file line by line using Java Streams API.
     *
     * @param file The file to read
     * @return A list of lines from the file
     * @throws IOException If there is an error reading the file
     */
    public List<String> readLinesStream(File file) throws IOException {
        try (Stream<String> lines = Files.lines(file.toPath(), StandardCharsets.UTF_8)) {
            return lines.collect(Collectors.toList());
        }
    }

    /**
     * Write content to a file.
     *
     * @param file The file to write to
     * @param content The content to write
     * @throws IOException If there is an error writing to the file
     */
    public void writeFile(File file, String content) throws IOException {
        Files.writeString(file.toPath(), content, StandardCharsets.UTF_8);
    }

    /**
     * Write lines to a file using BufferedWriter.
     *
     * @param file The file to write to
     * @param lines The lines to write
     * @throws IOException If there is an error writing to the file
     */
    public void writeLines(File file, List<String> lines) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, StandardCharsets.UTF_8))) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        }
    }

    /**
     * Process a file line by line with a given operation.
     * This is more memory-efficient for large files.
     *
     * @param inputFile The input file
     * @param outputFile The output file
     * @param lineProcessor The function to process each line
     * @throws IOException If there is an error reading or writing files
     */
    public void processFileByLine(File inputFile, File outputFile, LineProcessor lineProcessor) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(inputFile, StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputFile, StandardCharsets.UTF_8))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String processedLine = lineProcessor.process(line);
                writer.write(processedLine);
                writer.newLine();
            }
        }
    }

    /**
     * Process multiple files in batch with a given operation.
     *
     * @param inputFiles List of input files
     * @param outputDir Output directory
     * @param lineProcessor The function to process each line
     * @return Number of files processed successfully
     */
    public int batchProcessFiles(List<File> inputFiles, File outputDir, LineProcessor lineProcessor) {
        AtomicInteger successCount = new AtomicInteger(0);

        inputFiles.parallelStream().forEach(inputFile -> {
            try {
                String fileName = inputFile.getName();
                String baseName = fileName.substring(0, fileName.lastIndexOf('.'));
                File outputFile = new File(outputDir, baseName + "_processed.txt");

                processFileByLine(inputFile, outputFile, lineProcessor);
                successCount.incrementAndGet();
            } catch (IOException e) {
                // Log the error but continue processing other files
                System.err.println("Error processing file " + inputFile.getName() + ": " + e.getMessage());
            }
        });

        return successCount.get();
    }

    /**
     * Find files in a directory matching a filename pattern.
     *
     * @param directory The directory to search
     * @param pattern The glob pattern for filenames
     * @return List of matching files
     * @throws IOException If there is an error accessing the directory
     */
    public List<File> findFiles(File directory, String pattern) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }

        try (Stream<Path> paths = Files.find(directory.toPath(), Integer.MAX_VALUE,
                (path, attrs) -> path.toFile().isFile() &&
                        path.getFileName().toString().matches(pattern))) {
            return paths.map(Path::toFile).collect(Collectors.toList());
        }
    }

    /**
     * Interface for line processing operations.
     */
    @FunctionalInterface
    public interface LineProcessor {
        String process(String line);
    }
}
