package com.detector.report;

import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportGenerator Export & Formatting Tests")
public class ReportGeneratorTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should generate valid CSV report with header and account rows")
    void testGenerateCsvReport() throws IOException {
        ReportGenerator generator = new ReportGenerator(tempDir.toString());

        AnalysisResult r1 = new AnalysisResult("ACC_1", "user1", 75, RiskLevel.HIGH,
                Arrays.asList("NewAccountRule", "RapidActivityRule"),
                Arrays.asList("Young account", "Burst comments"));

        AnalysisResult r2 = new AnalysisResult("ACC_2", "user2", 15, RiskLevel.LOW,
                Collections.emptyList(), Collections.emptyList());

        File csvFile = generator.generateCsvReport(Arrays.asList(r1, r2));
        assertTrue(csvFile.exists(), "CSV report file must exist");
        assertTrue(csvFile.length() > 0, "CSV file must not be empty");

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFile))) {
            String header = reader.readLine();
            assertNotNull(header);
            assertTrue(header.contains("AccountId"));
            assertTrue(header.contains("RiskScore"));

            String row1 = reader.readLine();
            assertTrue(row1.contains("ACC_1"));
            assertTrue(row1.contains("HIGH"));
            assertTrue(row1.contains("75"));

            String row2 = reader.readLine();
            assertTrue(row2.contains("ACC_2"));
            assertTrue(row2.contains("LOW"));
            assertTrue(row2.contains("15"));
        }
    }

    @Test
    @DisplayName("Should generate comprehensive TXT audit summary with distribution and disclaimer")
    void testGenerateTxtReport() throws IOException {
        ReportGenerator generator = new ReportGenerator(tempDir.toString());

        AnalysisResult r1 = new AnalysisResult("ACC_1", "bot1", 85, RiskLevel.HIGH,
                Collections.singletonList("NewAccountRule"),
                Collections.singletonList("Account age is 5 days"));

        File txtFile = generator.generateTxtReport(Collections.singletonList(r1));
        assertTrue(txtFile.exists(), "TXT report file must exist");

        StringBuilder content = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new FileReader(txtFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                content.append(line).append("\n");
            }
        }

        String reportText = content.toString();
        assertTrue(reportText.contains("AUDIT REPORT"));
        assertTrue(reportText.contains("RISK DISTRIBUTION SUMMARY"));
        assertTrue(reportText.contains("HIGH RISK"));
        assertTrue(reportText.contains("bot1"));
        assertTrue(reportText.contains("IMPORTANT ETHICAL NOTICE & DISCLAIMER"));
    }
}
