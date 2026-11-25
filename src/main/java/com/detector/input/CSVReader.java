package com.detector.input;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;
import com.detector.validation.DataValidator;
import com.detector.validation.ValidationResult;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Reads, parses, and validates social media account datasets from CSV files.
 * Robustly handles missing lines, invalid formats, and skips header rows without crashing.
 */
public class CSVReader {
    public static final int EXPECTED_COLUMN_COUNT = 11;

    private final CsvLineParser lineParser;
    private final AccountDataMapper dataMapper;

    public CSVReader() {
        this(new CsvLineParser(), new AccountDataMapper());
    }

    public CSVReader(CsvLineParser lineParser, AccountDataMapper dataMapper) {
        this.lineParser = (lineParser != null) ? lineParser : new CsvLineParser();
        this.dataMapper = (dataMapper != null) ? dataMapper : new AccountDataMapper();
    }

    /**
     * Data transfer object reporting the outcome of a CSV import operation.
     */
    public static class CSVImportResult {
        private final List<SocialMediaAccount> accounts;
        private final List<String> errorMessages;
        private final int totalLinesProcessed;

        public CSVImportResult(List<SocialMediaAccount> accounts, List<String> errorMessages, int totalLinesProcessed) {
            this.accounts = accounts;
            this.errorMessages = errorMessages;
            this.totalLinesProcessed = totalLinesProcessed;
        }

        public List<SocialMediaAccount> getAccounts() {
            return Collections.unmodifiableList(accounts);
        }

        public List<String> getErrorMessages() {
            return Collections.unmodifiableList(errorMessages);
        }

        public int getTotalLinesProcessed() {
            return totalLinesProcessed;
        }

        public int getSuccessCount() {
            return accounts.size();
        }

        public int getFailureCount() {
            return errorMessages.size();
        }

        public boolean hasErrors() {
            return !errorMessages.isEmpty();
        }
    }

    /**
     * Reads a CSV file and converts valid rows into SocialMediaAccount objects.
     *
     * @param csvFile file to read
     * @return CSVImportResult containing loaded accounts and line error reports
     * @throws IOException if file cannot be found or read
     */
    public CSVImportResult readAccountsFromCSV(File csvFile) throws IOException {
        if (csvFile == null || !csvFile.exists()) {
            throw new IOException("CSV file not found: " + (csvFile != null ? csvFile.getPath() : "null"));
        }

        List<SocialMediaAccount> validAccounts = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int lineNumber = 0;

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String line;
            boolean isHeader = true;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                String trimmed = line.trim();

                // Skip empty lines or comment lines starting with #
                if (lineParser.isIgnorable(trimmed)) {
                    continue;
                }

                // Skip header row
                if (isHeader) {
                    isHeader = false;
                    // If first row looks like header, skip it
                    if (lineParser.isHeader(trimmed)) {
                        continue;
                    }
                }

                try {
                    SocialMediaAccount account = parseCsvRow(trimmed, lineNumber);
                    ValidationResult validation = DataValidator.validateAccount(account);

                    if (validation.isValid()) {
                        validAccounts.add(account);
                    } else {
                        errors.add(String.format("Line %d: Validation failed - %s",
                                lineNumber, validation.getFormattedErrors()));
                    }
                } catch (IllegalArgumentException e) {
                    errors.add(String.format("Line %d: Parse error - %s", lineNumber, e.getMessage()));
                }
            }
        }

        return new CSVImportResult(validAccounts, errors, lineNumber);
    }

    /**
     * Parses a single CSV line into a SocialMediaAccount object.
     */
    private SocialMediaAccount parseCsvRow(String line, int lineNumber) {
        String[] tokens = lineParser.parseLine(line, lineNumber);
        return dataMapper.mapTokens(tokens);
    }
}
