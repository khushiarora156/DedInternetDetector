package com.detector;

import com.detector.analysis.*;
import com.detector.input.CSVReader;
import com.detector.manager.AccountManager;
import com.detector.model.*;
import com.detector.report.ReportGenerator;
import com.detector.service.AnalysisService;
import com.detector.validation.DataValidator;
import com.detector.validation.ValidationResult;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;

/**
 * Standalone automated test runner executable directly with standard JDK (javac / java).
 * Does not require external build tools or third-party test runners, allowing 100% test verification
 * on systems where Maven is not yet configured on the PATH.
 */
public class TestHarness {
    private static int totalPassed = 0;
    private static int totalFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println("   DEAD INTERNET DETECTOR - STANDALONE VERIFICATION SUITE      ");
        System.out.println("===============================================================");

        runTest("Account Creation & Accessors", TestHarness::testAccountCreation);
        runTest("Follow Ratio Calculation", TestHarness::testFollowRatio);
        runTest("Engagement Calculation & Zero Followers Handling", TestHarness::testEngagementRate);
        runTest("Account Defensive Negative Validation", TestHarness::testNegativeMetricsValidation);
        runTest("DataValidator Validation Logic", TestHarness::testDataValidator);
        runTest("AccountManager Duplicate Prevention & O(1) Lookup", TestHarness::testAccountManager);
        runTest("NewAccountRule Edge Cases", TestHarness::testNewAccountRule);
        runTest("RepetitiveCommentRule & Min Sample Guardrail", TestHarness::testRepetitiveCommentRule);
        runTest("UnusualEngagementRule & Zero Follower Safety", TestHarness::testUnusualEngagementRule);
        runTest("RapidActivityRule & Boundary Threshold", TestHarness::testRapidActivityRule);
        runTest("SuspiciousFollowRatioRule & Newcomer Protection", TestHarness::testSuspiciousFollowRatioRule);
        runTest("RiskScoreCalculator Capping & Boundary Classification", TestHarness::testRiskScoreCalculator);
        runTest("Sequential vs Multithreaded Parallel Consistency", TestHarness::testMultithreadingConsistency);
        runTest("CSVReader Parsing & Malformed Line Resilience", TestHarness::testCSVReader);
        runTest("ReportGenerator CSV and TXT Export", TestHarness::testReportGenerator);

        System.out.println("===============================================================");
        System.out.printf("TEST SUITE SUMMARY: %d PASSED, %d FAILED%n", totalPassed, totalFailed);
        System.out.println("===============================================================");

        if (totalFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, Runnable testMethod) {
        try {
            testMethod.run();
            System.out.printf("[PASS] %s%n", testName);
            totalPassed++;
        } catch (Throwable t) {
            System.err.printf("[FAIL] %s - Cause: %s%n", testName, t.getMessage());
            t.printStackTrace(System.err);
            totalFailed++;
        }
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) throw new AssertionError("Assertion Failed: " + message);
    }

    private static void assertFalse(boolean condition, String message) {
        if (condition) throw new AssertionError("Assertion Failed (expected false): " + message);
    }

    private static void assertEquals(Object expected, Object actual, String message) {
        if (!Objects.equals(expected, actual)) {
            throw new AssertionError(String.format("Assertion Failed: %s [expected=%s, actual=%s]",
                    message, expected, actual));
        }
    }

    // --- Test Implementations ---

    private static void testAccountCreation() {
        AccountActivity act = new AccountActivity(45.0, 5.0, 4, 20, 3);
        SocialMediaAccount acc = new SocialMediaAccount("ACC_100", "testuser", 1000, 200, 150, 90, act);

        assertEquals("ACC_100", acc.getAccountId(), "Account ID");
        assertEquals("testuser", acc.getUsername(), "Username");
        assertEquals(1000, acc.getFollowers(), "Followers");
        assertEquals(200, acc.getFollowing(), "Following");
        assertEquals(150, acc.getTotalPosts(), "Total posts");
        assertEquals(90, acc.getAccountAgeDays(), "Account age");
        assertEquals(0.2, acc.getFollowRatio(), "Follow ratio");
    }

    private static void testFollowRatio() {
        AccountActivity act = new AccountActivity(10, 2, 0, 5, 1);
        SocialMediaAccount normal = new SocialMediaAccount("A1", "user1", 1000, 250, 10, 50, act);
        assertEquals(0.25, normal.getFollowRatio(), "250/1000 = 0.25");

        SocialMediaAccount zeroFollowers = new SocialMediaAccount("A2", "user2", 0, 80, 5, 20, act);
        assertEquals(80.0, zeroFollowers.getFollowRatio(), "Zero followers returns following count");
    }

    private static void testEngagementRate() {
        AccountActivity act = new AccountActivity(50.0, 10.0, 2, 10, 1);
        SocialMediaAccount acc = new SocialMediaAccount("A1", "user1", 1000, 100, 50, 60, act);
        // (50 + 10) / 1000 * 100 = 6.0%
        assertEquals(6.0, acc.getEngagementRate(), "Engagement rate calculation");

        SocialMediaAccount zeroFol = new SocialMediaAccount("A2", "user2", 0, 50, 5, 10, act);
        assertEquals(0.0, zeroFol.getEngagementRate(), "Zero followers returns 0.0 without crash");
    }

    private static void testNegativeMetricsValidation() {
        boolean caught = false;
        try {
            new SocialMediaAccount("A1", "u1", -10, 50, 5, 10, new AccountActivity());
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Negative followers must throw IllegalArgumentException");

        caught = false;
        try {
            new AccountActivity(-5.0, 2.0, 1, 5, 1);
        } catch (IllegalArgumentException e) {
            caught = true;
        }
        assertTrue(caught, "Negative average likes must throw IllegalArgumentException");
    }

    private static void testDataValidator() {
        AccountActivity act = new AccountActivity(20.0, 5.0, 2, 10, 3);
        SocialMediaAccount validAcc = new SocialMediaAccount("ACC_01", "valid_user", 500, 300, 100, 200, act);
        ValidationResult res1 = DataValidator.validateAccount(validAcc);
        assertTrue(res1.isValid(), "Valid account must pass");

        ValidationResult resNull = DataValidator.validateAccount(null);
        assertFalse(resNull.isValid(), "Null account must fail");

        // Repeated comments > total observed comments
        AccountActivity badAct = new AccountActivity(10, 2, 25, 20, 1);
        SocialMediaAccount badAcc = new SocialMediaAccount("ACC_02", "bot", 100, 50, 10, 20, badAct);
        ValidationResult resBad = DataValidator.validateAccount(badAcc);
        assertFalse(resBad.isValid(), "Repeated comments > total observed must fail");
    }

    private static void testAccountManager() {
        AccountManager mgr = new AccountManager();
        AccountActivity act = new AccountActivity(10, 2, 0, 5, 1);
        SocialMediaAccount acc1 = new SocialMediaAccount("ACC_01", "alice", 500, 100, 50, 200, act);
        SocialMediaAccount acc2 = new SocialMediaAccount("ACC_02", "bob", 800, 200, 75, 300, act);

        assertTrue(mgr.addAccount(acc1), "Add acc1");
        assertTrue(mgr.addAccount(acc2), "Add acc2");
        assertEquals(2, mgr.getAccountCount(), "Account count == 2");

        // Duplicate rejection
        SocialMediaAccount duplicate = new SocialMediaAccount("ACC_01", "imposter", 10, 20, 5, 2, act);
        assertFalse(mgr.addAccount(duplicate), "Duplicate ID must be rejected");
        assertEquals(2, mgr.getAccountCount(), "Count must remain 2");

        // O(1) Lookup
        assertTrue(mgr.findAccountById("ACC_02").isPresent(), "Find ACC_02");
        assertEquals("bob", mgr.findAccountById("ACC_02").get().getUsername(), "Match bob");

        // Removal
        assertTrue(mgr.removeAccount("ACC_01"), "Remove ACC_01");
        assertEquals(1, mgr.getAccountCount(), "Count is now 1");
        assertFalse(mgr.accountExists("ACC_01"), "ACC_01 does not exist");
    }

    private static void testNewAccountRule() {
        NewAccountRule rule = new NewAccountRule(30, 15);
        SocialMediaAccount young = new SocialMediaAccount("A1", "u1", 100, 50, 10, 10, new AccountActivity());
        assertTrue(rule.isTriggered(young), "Young account (<30d) triggers");

        SocialMediaAccount boundary = new SocialMediaAccount("A2", "u2", 100, 50, 10, 30, new AccountActivity());
        assertFalse(rule.isTriggered(boundary), "30d boundary does not trigger");

        SocialMediaAccount old = new SocialMediaAccount("A3", "u3", 100, 50, 10, 100, new AccountActivity());
        assertFalse(rule.isTriggered(old), "Old account does not trigger");
    }

    private static void testRepetitiveCommentRule() {
        RepetitiveCommentRule rule = new RepetitiveCommentRule(0.50, 5, 20);

        // 18 / 20 = 90% -> triggered
        AccountActivity actSpam = new AccountActivity(10, 2, 18, 20, 2);
        assertTrue(rule.isTriggered(new SocialMediaAccount("A1", "s", 100, 50, 10, 100, actSpam)), "High repetition triggers");

        // 2 / 20 = 10% -> not triggered
        AccountActivity actNorm = new AccountActivity(10, 2, 2, 20, 2);
        assertFalse(rule.isTriggered(new SocialMediaAccount("A2", "n", 100, 50, 10, 100, actNorm)), "Low repetition does not trigger");

        // 2 / 2 = 100%, but sample size 2 < min 5 -> guardrail prevents false positive
        AccountActivity actSmall = new AccountActivity(10, 2, 2, 2, 1);
        assertFalse(rule.isTriggered(new SocialMediaAccount("A3", "sm", 100, 50, 10, 100, actSmall)), "Small sample size does not trigger");
    }

    private static void testUnusualEngagementRule() {
        UnusualEngagementRule rule = new UnusualEngagementRule(25.0, 15);

        // 100 followers, avg 60 interactions = 60% engagement (> 25%) -> triggered
        AccountActivity actHigh = new AccountActivity(50, 10, 0, 5, 1);
        assertTrue(rule.isTriggered(new SocialMediaAccount("A1", "b", 100, 50, 10, 100, actHigh)), "60% engagement triggers");

        // 1000 followers, 12 interactions = 1.2% -> not triggered
        AccountActivity actNorm = new AccountActivity(10, 2, 0, 5, 1);
        assertFalse(rule.isTriggered(new SocialMediaAccount("A2", "h", 1000, 50, 10, 100, actNorm)), "1.2% engagement does not trigger");

        // 0 followers with interactions -> synthetic activity -> triggered
        assertTrue(rule.isTriggered(new SocialMediaAccount("A3", "z1", 0, 10, 5, 10, actHigh)), "0 followers with likes triggers");

        // 0 followers with 0 interactions -> genuine new account -> not triggered
        assertFalse(rule.isTriggered(new SocialMediaAccount("A4", "z2", 0, 5, 1, 1, new AccountActivity())), "0 followers 0 likes does not trigger");
    }

    private static void testRapidActivityRule() {
        RapidActivityRule rule = new RapidActivityRule(20, 15);

        AccountActivity actBurst = new AccountActivity(5, 1, 0, 30, 25);
        assertTrue(rule.isTriggered(new SocialMediaAccount("A1", "f", 200, 100, 50, 100, actBurst)), "25 comments in 10m triggers");

        AccountActivity actBoundary = new AccountActivity(5, 1, 0, 30, 20);
        assertFalse(rule.isTriggered(new SocialMediaAccount("A2", "b", 200, 100, 50, 100, actBoundary)), "20 comments boundary does not trigger");

        AccountActivity actHuman = new AccountActivity(5, 1, 0, 30, 3);
        assertFalse(rule.isTriggered(new SocialMediaAccount("A3", "h", 200, 100, 50, 100, actHuman)), "3 comments does not trigger");
    }

    private static void testSuspiciousFollowRatioRule() {
        SuspiciousFollowRatioRule rule = new SuspiciousFollowRatioRule(10.0, 150, 15);

        // 2500 following, 50 followers (ratio 50x) -> triggered
        SocialMediaAccount spammer = new SocialMediaAccount("A1", "s", 50, 2500, 10, 50, new AccountActivity());
        assertTrue(rule.isTriggered(spammer), "50x follow ratio with 2500 following triggers");

        // 200 following, 400 followers (ratio 0.5x) -> not triggered
        SocialMediaAccount normal = new SocialMediaAccount("A2", "n", 400, 200, 10, 50, new AccountActivity());
        assertFalse(rule.isTriggered(normal), "Normal ratio does not trigger");

        // 50 following, 2 followers (ratio 25x, but following < 150) -> newcomer guardrail
        SocialMediaAccount small = new SocialMediaAccount("A3", "p", 2, 50, 1, 5, new AccountActivity());
        assertFalse(rule.isTriggered(small), "Small account protected by min following guardrail");
    }

    private static void testRiskScoreCalculator() {
        List<DetectionRule> rules = Arrays.asList(
                new NewAccountRule(30, 15),
                new RepetitiveCommentRule(0.5, 5, 20),
                new RapidActivityRule(20, 15)
        );
        assertEquals(50, RiskScoreCalculator.calculateScore(rules), "15 + 20 + 15 = 50");

        // Capping at 100
        assertEquals(100, RiskScoreCalculator.capScore(150), "Cap 150 at 100");
        assertEquals(0, RiskScoreCalculator.capScore(-10), "Floor -10 at 0");

        // Classifications
        assertEquals(RiskLevel.LOW, RiskScoreCalculator.determineRiskLevel(0), "0 is LOW");
        assertEquals(RiskLevel.LOW, RiskScoreCalculator.determineRiskLevel(29), "29 is LOW");
        assertEquals(RiskLevel.MEDIUM, RiskScoreCalculator.determineRiskLevel(30), "30 is MEDIUM");
        assertEquals(RiskLevel.MEDIUM, RiskScoreCalculator.determineRiskLevel(59), "59 is MEDIUM");
        assertEquals(RiskLevel.HIGH, RiskScoreCalculator.determineRiskLevel(60), "60 is HIGH");
        assertEquals(RiskLevel.HIGH, RiskScoreCalculator.determineRiskLevel(100), "100 is HIGH");
    }

    private static void testMultithreadingConsistency() {
        AnalysisService service = new AnalysisService();
        service.registerAccount(new SocialMediaAccount("ACC_01", "normal1", 1000, 200, 50, 400,
                new AccountActivity(20, 2, 1, 20, 2)));
        service.registerAccount(new SocialMediaAccount("ACC_02", "bot_new", 50, 2500, 10, 12,
                new AccountActivity(2, 0.5, 18, 20, 28)));
        service.registerAccount(new SocialMediaAccount("ACC_03", "booster", 100, 300, 25, 200,
                new AccountActivity(80, 40, 2, 20, 3)));
        service.registerAccount(new SocialMediaAccount("ACC_04", "spammer", 300, 200, 40, 150,
                new AccountActivity(5, 1, 0, 30, 35)));

        List<AnalysisResult> seq = service.analyzeAllSequential();
        List<AnalysisResult> par = service.analyzeAllParallel(4);

        assertEquals(seq.size(), par.size(), "Result count match");

        Map<String, AnalysisResult> seqMap = new HashMap<>();
        for (AnalysisResult r : seq) seqMap.put(r.getAccountId(), r);

        for (AnalysisResult p : par) {
            AnalysisResult s = seqMap.get(p.getAccountId());
            assertTrue(s != null, "Sequential result exists for " + p.getAccountId());
            assertEquals(s.getRiskScore(), p.getRiskScore(), "Risk score must match for " + p.getAccountId());
            assertEquals(s.getRiskLevel(), p.getRiskLevel(), "Risk level must match for " + p.getAccountId());
            assertEquals(s.getTriggeredRules(), p.getTriggeredRules(), "Triggered rules must match for " + p.getAccountId());
        }
    }

    private static void testCSVReader() {
        try {
            File tempCsv = File.createTempFile("test_accounts", ".csv");
            tempCsv.deleteOnExit();

            try (PrintWriter pw = new PrintWriter(new FileWriter(tempCsv))) {
                pw.println("accountId,username,followers,following,totalPosts,accountAgeDays,averageLikes,averageComments,repeatedComments,totalCommentsObserved,commentsInTenMinutes");
                pw.println("ACC_1,user_one,100,20,5,50,10.0,2.0,1,5,0");
                pw.println("ACC_2,bad_columns,100,20");
                pw.println("ACC_3,user_three,200,40,10,120,25.0,4.0,2,10,1");
            }

            CSVReader reader = new CSVReader();
            CSVReader.CSVImportResult result = reader.readAccountsFromCSV(tempCsv);

            assertEquals(2, result.getSuccessCount(), "2 valid rows parsed");
            assertEquals(1, result.getFailureCount(), "1 invalid row captured");
            assertTrue(result.hasErrors(), "Has errors flag is true");
        } catch (Exception e) {
            throw new RuntimeException("CSVReader test failed: " + e.getMessage(), e);
        }
    }

    private static void testReportGenerator() {
        try {
            File tempDir = new File("reports_test_scratch");
            tempDir.mkdirs();
            tempDir.deleteOnExit();

            ReportGenerator generator = new ReportGenerator(tempDir.getPath());
            AnalysisResult r1 = new AnalysisResult("ACC_1", "user1", 75, RiskLevel.HIGH,
                    Arrays.asList("NewAccountRule"), Arrays.asList("Young account"));
            AnalysisResult r2 = new AnalysisResult("ACC_2", "user2", 10, RiskLevel.LOW,
                    Collections.emptyList(), Collections.emptyList());

            File csvFile = generator.generateCsvReport(Arrays.asList(r1, r2));
            File txtFile = generator.generateTxtReport(Arrays.asList(r1, r2));

            assertTrue(csvFile.exists() && csvFile.length() > 0, "CSV report exists and non-empty");
            assertTrue(txtFile.exists() && txtFile.length() > 0, "TXT report exists and non-empty");

            // Clean up
            csvFile.delete();
            txtFile.delete();
            tempDir.delete();
        } catch (Exception e) {
            throw new RuntimeException("ReportGenerator test failed: " + e.getMessage(), e);
        }
    }
}
