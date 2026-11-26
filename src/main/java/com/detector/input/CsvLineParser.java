package com.detector.input;

/**
 * Handles CSV row splitting, token trimming, and structural validation.
 * Decouples low-level tokenization from domain object mapping.
 */
public class CsvLineParser {
    public static final int EXPECTED_COLUMN_COUNT = 11;

    /**
     * Splits a CSV line into an array of trimmed tokens and validates column count.
     *
     * @param line       raw CSV line
     * @param lineNumber line number for context in error messages
     * @return array of trimmed string tokens
     * @throws IllegalArgumentException if token count does not meet expected columns
     */
    public String[] parseLine(String line, int lineNumber) {
        if (line == null) {
            throw new IllegalArgumentException(String.format("Line %d is null.", lineNumber));
        }

        String[] tokens = line.split(",", -1);
        if (tokens.length < EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException(String.format(
                    "Expected %d columns, but found %d columns.", EXPECTED_COLUMN_COUNT, tokens.length));
        }

        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = tokens[i].trim();
        }

        return tokens;
    }

    /**
     * Determines whether a line is empty or a comment line.
     *
     * @param line line to inspect
     * @return true if comment or empty
     */
    public boolean isIgnorable(String line) {
        if (line == null) {
            return true;
        }
        String trimmed = line.trim();
        return trimmed.isEmpty() || trimmed.startsWith("#");
    }

    /**
     * Determines whether a line represents the CSV header.
     *
     * @param line line to inspect
     * @return true if line begins with an identifier header
     */
    public boolean isHeader(String line) {
        if (line == null) {
            return false;
        }
        String lower = line.trim().toLowerCase();
        return lower.startsWith("accountid") || lower.startsWith("id");
    }
}
