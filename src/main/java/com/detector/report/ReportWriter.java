package com.detector.report;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Objects;

/**
 * Encapsulates safe file persistence for generated reports.
 * Ensures parent directories are created defensively and manages file streams with try-with-resources.
 */
public class ReportWriter {
    private final String outputDirectory;

    public ReportWriter(String outputDirectory) {
        this.outputDirectory = Objects.requireNonNull(outputDirectory, "Output directory cannot be null");
        ensureOutputDirectoryExists();
    }

    public void ensureOutputDirectoryExists() {
        File dir = new File(outputDirectory);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    /**
     * Writes textual content to a target file within the configured output directory.
     *
     * @param filename target file name
     * @param content  text content to write
     * @return created File object
     * @throws IOException if writing fails
     */
    public File writeReport(String filename, String content) throws IOException {
        ensureOutputDirectoryExists();
        File targetFile = new File(outputDirectory, filename);

        try (PrintWriter writer = new PrintWriter(new FileWriter(targetFile))) {
            writer.print(content);
            writer.flush();
        }

        return targetFile;
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
}
