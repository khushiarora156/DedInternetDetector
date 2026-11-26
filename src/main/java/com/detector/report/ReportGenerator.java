package com.detector.report;

import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Generates formatted CSV and TXT audit reports from analysis results.
 * Manages the destination 'reports/' directory and ensures safe file I/O operations.
 */
public class ReportGenerator {
    public static final String DEFAULT_REPORTS_DIR = "reports";
    private static final DateTimeFormatter FILE_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final DateTimeFormatter DISPLAY_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final String outputDirectory;
    private final CsvReportFormatter csvReportFormatter;
    private final TxtReportFormatter txtReportFormatter;
    private final ReportWriter reportWriter;

    public ReportGenerator() {
        this(DEFAULT_REPORTS_DIR);
    }

    public ReportGenerator(String outputDirectory) {
        this(outputDirectory, new CsvReportFormatter(), new TxtReportFormatter(), new ReportWriter(outputDirectory));
    }

    public ReportGenerator(String outputDirectory, CsvReportFormatter csvReportFormatter,
                           TxtReportFormatter txtReportFormatter, ReportWriter reportWriter) {
        this.outputDirectory = outputDirectory;
        this.csvReportFormatter = (csvReportFormatter != null) ? csvReportFormatter : new CsvReportFormatter();
        this.txtReportFormatter = (txtReportFormatter != null) ? txtReportFormatter : new TxtReportFormatter();
        this.reportWriter = (reportWriter != null) ? reportWriter : new ReportWriter(outputDirectory);
        ensureOutputDirectoryExists();
    }

    private void ensureOutputDirectoryExists() {
        if (reportWriter != null) {
            reportWriter.ensureOutputDirectoryExists();
        } else {
            File dir = new File(outputDirectory);
            if (!dir.exists()) {
                dir.mkdirs();
            }
        }
    }

    /**
     * Exports analysis results to a CSV file.
     *
     * @param results List of AnalysisResult objects
     * @return generated File
     * @throws IOException if writing fails
     */
    public File generateCsvReport(List<AnalysisResult> results) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
        String filename = String.format("analysis_report_%s.csv", timestamp);
        String content = csvReportFormatter.format(results);
        return reportWriter.writeReport(filename, content);
    }

    /**
     * Exports a comprehensive, human-readable audit summary report to a TXT file.
     *
     * @param results List of AnalysisResult objects
     * @return generated File
     * @throws IOException if writing fails
     */
    public File generateTxtReport(List<AnalysisResult> results) throws IOException {
        String timestamp = LocalDateTime.now().format(FILE_TS_FORMATTER);
        String filename = String.format("analysis_summary_%s.txt", timestamp);
        ReportMetadata metadata = new ReportMetadata(results != null ? results.size() : 0);
        String content = txtReportFormatter.format(results, metadata);
        return reportWriter.writeReport(filename, content);
    }

    public String getOutputDirectory() {
        return outputDirectory;
    }
}
