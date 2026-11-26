package com.detector.input;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("CSVReader Parsing & Fault Tolerance Tests")
public class CSVReaderTest {

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("Should parse valid CSV file skipping header correctly")
    void testValidCsvImport() throws IOException {
        File csvFile = tempDir.resolve("valid.csv").toFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            pw.println("accountId,username,followers,following,totalPosts,accountAgeDays,averageLikes,averageComments,repeatedComments,totalCommentsObserved,commentsInTenMinutes");
            pw.println("ACC_1,user_one,100,20,5,50,10.0,2.0,1,5,0");
            pw.println("ACC_2,user_two,200,40,10,120,25.0,4.0,2,10,1");
        }

        CSVReader reader = new CSVReader();
        CSVReader.CSVImportResult result = reader.readAccountsFromCSV(csvFile);

        assertEquals(2, result.getSuccessCount());
        assertEquals(0, result.getFailureCount());
        assertFalse(result.hasErrors());
        assertEquals("user_one", result.getAccounts().get(0).getUsername());
        assertEquals("user_two", result.getAccounts().get(1).getUsername());
    }

    @Test
    @DisplayName("Should capture errors on malformed rows without crashing")
    void testMalformedCsvRows() throws IOException {
        File csvFile = tempDir.resolve("malformed.csv").toFile();
        try (PrintWriter pw = new PrintWriter(new FileWriter(csvFile))) {
            pw.println("accountId,username,followers,following,totalPosts,accountAgeDays,averageLikes,averageComments,repeatedComments,totalCommentsObserved,commentsInTenMinutes");
            pw.println("ACC_1,valid_user,100,20,5,50,10.0,2.0,1,5,0"); // valid
            pw.println("ACC_2,missing_columns,100,20");                // too few columns
            pw.println("ACC_3,bad_number,abc,20,5,50,10.0,2.0,1,5,0");   // non-numeric follower
            pw.println("ACC_4,neg_posts,100,20,-5,50,10.0,2.0,1,5,0");    // negative post count
        }

        CSVReader reader = new CSVReader();
        CSVReader.CSVImportResult result = reader.readAccountsFromCSV(csvFile);

        assertEquals(1, result.getSuccessCount());
        assertEquals(3, result.getFailureCount());
        assertTrue(result.hasErrors());
        assertEquals("valid_user", result.getAccounts().get(0).getUsername());
    }

    @Test
    @DisplayName("Should throw IOException on missing file")
    void testMissingFile() {
        CSVReader reader = new CSVReader();
        File nonExistent = new File(tempDir.toFile(), "does_not_exist.csv");

        assertThrows(IOException.class, () -> reader.readAccountsFromCSV(nonExistent));
    }
}
