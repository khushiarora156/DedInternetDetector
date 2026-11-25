package com.detector.report;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Encapsulates contextual metadata for generated audit reports.
 */
public class ReportMetadata {
    public static final String APPLICATION_NAME = "Dead Internet & Fake Engagement Detector";
    public static final String APPLICATION_VERSION = "1.0.0";
    public static final DateTimeFormatter DISPLAY_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    public static final DateTimeFormatter FILE_TS_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private final LocalDateTime generatedAt;
    private final int totalRecords;
    private final String generatedBy;

    public ReportMetadata(int totalRecords) {
        this(LocalDateTime.now(), totalRecords, APPLICATION_NAME + " v" + APPLICATION_VERSION);
    }

    public ReportMetadata(LocalDateTime generatedAt, int totalRecords, String generatedBy) {
        this.generatedAt = (generatedAt != null) ? generatedAt : LocalDateTime.now();
        this.totalRecords = Math.max(0, totalRecords);
        this.generatedBy = (generatedBy != null) ? generatedBy : APPLICATION_NAME;
    }

    public LocalDateTime getGeneratedAt() {
        return generatedAt;
    }

    public String getFormattedDisplayTime() {
        return generatedAt.format(DISPLAY_FORMATTER);
    }

    public String getFileTimestamp() {
        return generatedAt.format(FILE_TS_FORMATTER);
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public String getGeneratedBy() {
        return generatedBy;
    }
}
