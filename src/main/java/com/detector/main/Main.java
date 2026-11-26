package com.detector.main;

import com.detector.analysis.DetectionRule;
import com.detector.analytics.RiskDistributionSummary;
import com.detector.analytics.RiskStatisticsCalculator;
import com.detector.input.CSVReader;
import com.detector.input.ManualInput;
import com.detector.model.AccountActivity;
import com.detector.model.AnalysisResult;
import com.detector.model.SocialMediaAccount;
import com.detector.report.ReportGenerator;
import com.detector.service.AnalysisService;
import com.detector.validation.ValidationResult;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * Entry point and interactive console interface for the Dead Internet & Fake Engagement Detector.
 * Keeps business logic decoupled by delegating to AnalysisService, CSVReader, ManualInput, and ReportGenerator.
 */
public class Main {
    private static final String DEFAULT_CSV_PATH = "data/accounts.csv";

    private final AnalysisService analysisService;
    private final CSVReader csvReader;
    private final ManualInput manualInput;
    private final ReportGenerator reportGenerator;
    private final Scanner scanner;

    public Main() {
        this.analysisService = new AnalysisService();
        this.csvReader = new CSVReader();
        this.scanner = new Scanner(System.in);
        this.manualInput = new ManualInput(scanner);
        this.reportGenerator = new ReportGenerator();
    }

    public static void main(String[] args) {
        Main app = new Main();
        app.run();
    }

    /**
     * Main application loop.
     */
    public void run() {
        printBanner();
        boolean running = true;

        while (running) {
            printMenu();
            System.out.print("Enter your choice (1-12): ");
            String input = scanner.nextLine().trim();

            System.out.println();
            switch (input) {
                case "1":
                    handleAddAccountManually();
                    break;
                case "2":
                    handleImportCsv();
                    break;
                case "3":
                    handleViewAllAccounts();
                    break;
                case "4":
                    handleSearchAccountById();
                    break;
                case "5":
                    handleAnalyzeSequential();
                    break;
                case "6":
                    handleAnalyzeParallel();
                    break;
                case "7":
                    handleViewRankedAccounts();
                    break;
                case "8":
                    handleViewHighRiskAccounts();
                    break;
                case "9":
                    handleGenerateCsvReport();
                    break;
                case "10":
                    handleGenerateTxtReport();
                    break;
                case "11":
                    handleViewDetectionRules();
                    break;
                case "12":
                    running = false;
                    System.out.println("=================================================");
                    System.out.println("  Thank you for using Dead Internet Detector.   ");
                    System.out.println("  Stay vigilant against automated manipulation!  ");
                    System.out.println("=================================================");
                    break;
                default:
                    System.out.println("[!] Invalid choice. Please enter a number between 1 and 12.");
            }

            if (running) {
                System.out.println("\nPress [ENTER] to return to the menu...");
                scanner.nextLine();
            }
        }
    }

    private void printBanner() {
        System.out.println("============================================================");
        System.out.println("        DEAD INTERNET & FAKE ENGAGEMENT DETECTOR            ");
        System.out.println("   Auditing Social Media Accounts for Bot Activity Patterns ");
        System.out.println("============================================================");
    }

    private void printMenu() {
        System.out.println("\n========================================");
        System.out.println(" DEAD INTERNET & FAKE ENGAGEMENT DETECTOR");
        System.out.println("========================================");
        System.out.println(" 1. Add Account Manually");
        System.out.println(" 2. Import Accounts from CSV");
        System.out.println(" 3. View All Accounts");
        System.out.println(" 4. Search Account by ID");
        System.out.println(" 5. Analyze All Accounts");
        System.out.println(" 6. Analyze Accounts in Parallel");
        System.out.println(" 7. View Ranked Accounts");
        System.out.println(" 8. View High-Risk Accounts");
        System.out.println(" 9. Generate CSV Report");
        System.out.println("10. Generate TXT Report");
        System.out.println("11. View Detection Rules");
        System.out.println("12. Exit");
        System.out.println("========================================");
    }

    // --- Menu Action Handlers ---

    private void handleAddAccountManually() {
        SocialMediaAccount account = manualInput.promptForAccount();
        if (account == null) {
            return;
        }

        ValidationResult result = analysisService.registerAccount(account);
        if (result.isValid()) {
            System.out.printf("[✓] Account '@%s' (ID: %s) successfully registered!%n",
                    account.getUsername(), account.getAccountId());
        } else {
            System.out.printf("[!] Could not register account: %s%n", result.getFormattedErrors());
        }
    }

    private void handleImportCsv() {
        System.out.printf("Enter CSV file path [default: %s]: ", DEFAULT_CSV_PATH);
        String inputPath = scanner.nextLine().trim();
        if (inputPath.isEmpty()) {
            inputPath = DEFAULT_CSV_PATH;
        }

        File file = new File(inputPath);
        if (!file.exists()) {
            System.out.printf("[!] File not found: %s%n", file.getAbsolutePath());
            return;
        }

        try {
            System.out.printf("[*] Reading accounts from '%s'...%n", file.getName());
            CSVReader.CSVImportResult importResult = csvReader.readAccountsFromCSV(file);

            int registeredCount = 0;
            int duplicateCount = 0;

            for (SocialMediaAccount acc : importResult.getAccounts()) {
                ValidationResult reg = analysisService.registerAccount(acc);
                if (reg.isValid()) {
                    registeredCount++;
                } else {
                    duplicateCount++;
                }
            }

            System.out.println("\n---------------- CSV IMPORT SUMMARY ----------------");
            System.out.printf("  Lines Processed      : %d%n", importResult.getTotalLinesProcessed());
            System.out.printf("  Parsed Successfully  : %d%n", importResult.getSuccessCount());
            System.out.printf("  Registered (New)     : %d%n", registeredCount);
            System.out.printf("  Duplicates Skipped   : %d%n", duplicateCount);
            System.out.printf("  Malformed / Failed   : %d%n", importResult.getFailureCount());

            if (importResult.hasErrors()) {
                System.out.println("\n[!] Parsing Issues Encountered:");
                for (String err : importResult.getErrorMessages()) {
                    System.out.println("    - " + err);
                }
            }
            System.out.println("----------------------------------------------------");

        } catch (IOException e) {
            System.out.printf("[!] Error reading CSV file: %s%n", e.getMessage());
        }
    }

    private void handleViewAllAccounts() {
        List<SocialMediaAccount> accounts = analysisService.getAccountManager().getAllAccounts();
        if (accounts.isEmpty()) {
            System.out.println("[*] No accounts are currently loaded. Import a CSV or add an account manually.");
            return;
        }

        System.out.println("==========================================================================================");
        System.out.printf("%-10s | %-16s | %-10s | %-10s | %-6s | %-6s | %s%n",
                "ACCOUNT ID", "USERNAME", "FOLLOWERS", "FOLLOWING", "POSTS", "AGE(d)", "ACTIVITY METRICS");
        System.out.println("==========================================================================================");

        for (SocialMediaAccount acc : accounts) {
            AccountActivity act = acc.getActivity();
            String actSummary = String.format("Likes:%.1f, Cmts:%.1f, Rep:%d/%d, Burst:%d",
                    act.getAverageLikes(), act.getAverageComments(),
                    act.getRepeatedComments(), act.getTotalCommentsObserved(),
                    act.getCommentsInTenMinutes());

            System.out.printf("%-10s | @%-15s | %,10d | %,10d | %,6d | %,6d | %s%n",
                    acc.getAccountId(), acc.getUsername(), acc.getFollowers(),
                    acc.getFollowing(), acc.getTotalPosts(), acc.getAccountAgeDays(), actSummary);
        }
        System.out.println("==========================================================================================");
        System.out.printf("Total registered accounts: %d%n", accounts.size());
    }

    private void handleSearchAccountById() {
        System.out.print("Enter Account ID to search: ");
        String searchId = scanner.nextLine().trim();
        if (searchId.isEmpty()) {
            System.out.println("[!] Account ID cannot be blank.");
            return;
        }

        Optional<SocialMediaAccount> found = analysisService.getAccountManager().findAccountById(searchId);
        if (found.isEmpty()) {
            System.out.printf("[!] No account found with ID '%s'.%n", searchId);
            return;
        }

        SocialMediaAccount acc = found.get();
        System.out.println("\n================ ACCOUNT DETAILS ================");
        System.out.printf("  Account ID       : %s%n", acc.getAccountId());
        System.out.printf("  Username         : @%s%n", acc.getUsername());
        System.out.printf("  Followers        : %,d%n", acc.getFollowers());
        System.out.printf("  Following        : %,d%n", acc.getFollowing());
        System.out.printf("  Follow Ratio     : %.2fx%n", acc.getFollowRatio());
        System.out.printf("  Total Posts      : %,d%n", acc.getTotalPosts());
        System.out.printf("  Account Age      : %d days%n", acc.getAccountAgeDays());
        System.out.printf("  Engagement Rate  : %.2f%%%n", acc.getEngagementRate());
        System.out.println("---------------- Activity Data -----------------");
        AccountActivity act = acc.getActivity();
        System.out.printf("  Avg Likes/Post   : %.1f%n", act.getAverageLikes());
        System.out.printf("  Avg Comments/Post: %.1f%n", act.getAverageComments());
        System.out.printf("  Repeated Comments: %d / %d (%.1f%%)%n",
                act.getRepeatedComments(), act.getTotalCommentsObserved(), act.getRepetitiveCommentRatio() * 100.0);
        System.out.printf("  Burst 10m Comments: %d%n", act.getCommentsInTenMinutes());

        // Check if analysis result exists
        Optional<AnalysisResult> resultOpt = analysisService.getResultForAccount(acc.getAccountId());
        if (resultOpt.isPresent()) {
            AnalysisResult res = resultOpt.get();
            System.out.println("---------------- Analysis Status ---------------");
            System.out.printf("  Risk Score       : %d / 100%n", res.getRiskScore());
            System.out.printf("  Risk Level       : %s%n", res.getRiskLevel().getDescription());
            System.out.printf("  Triggered Rules  : %s%n", res.getTriggeredRules().isEmpty() ? "None" : String.join(", ", res.getTriggeredRules()));
            for (String expl : res.getExplanations()) {
                System.out.println("    * " + expl);
            }
        } else {
            System.out.println("---------------- Analysis Status ---------------");
            System.out.println("  (Account has not been analyzed yet. Run option 5 or 6).");
        }
        System.out.println("=================================================");
    }

    private void handleAnalyzeSequential() {
        if (analysisService.getAccountCount() == 0) {
            System.out.println("[!] No accounts to analyze. Import CSV or add an account first.");
            return;
        }

        System.out.printf("[*] Running sequential (single-threaded) analysis on %d accounts...%n",
                analysisService.getAccountCount());
        List<AnalysisResult> results = analysisService.analyzeAllSequential();

        System.out.printf("[✓] Sequential analysis completed in %d ms!%n",
                analysisService.getLastAnalysisDurationMs());
        displayAnalysisSummary(results);
    }

    private void handleAnalyzeParallel() {
        if (analysisService.getAccountCount() == 0) {
            System.out.println("[!] No accounts to analyze. Import CSV or add an account first.");
            return;
        }

        int defaultThreads = Math.min(Runtime.getRuntime().availableProcessors(), analysisService.getAccountCount());
        System.out.printf("Enter number of worker threads [default: %d, available CPUs: %d]: ",
                defaultThreads, Runtime.getRuntime().availableProcessors());
        String threadInput = scanner.nextLine().trim();

        int threads = defaultThreads;
        if (!threadInput.isEmpty()) {
            try {
                threads = Integer.parseInt(threadInput);
                if (threads <= 0) threads = defaultThreads;
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number; using default thread count.");
                threads = defaultThreads;
            }
        }

        System.out.printf("[*] Running parallel analysis with %d worker thread(s) on %d accounts...%n",
                threads, analysisService.getAccountCount());
        List<AnalysisResult> results = analysisService.analyzeAllParallel(threads);

        System.out.printf("[✓] Parallel analysis completed in %d ms using ExecutorService!%n",
                analysisService.getLastAnalysisDurationMs());
        displayAnalysisSummary(results);
    }

    private void displayAnalysisSummary(List<AnalysisResult> results) {
        RiskDistributionSummary summary = RiskStatisticsCalculator.calculateSummary(results);

        System.out.println("\n---------------- QUICK SUMMARY ----------------");
        System.out.printf("  HIGH RISK   (Score 60-100) : %d%n", summary.getHighRiskCount());
        System.out.printf("  MEDIUM RISK (Score 30-59)  : %d%n", summary.getMediumRiskCount());
        System.out.printf("  LOW RISK    (Score 00-29)  : %d%n", summary.getLowRiskCount());
        System.out.println("  (Use Option 7 to view ranked accounts or Option 8 for high-risk details)");
        System.out.println("-----------------------------------------------");
    }

    private void handleViewRankedAccounts() {
        List<AnalysisResult> results = ensureAnalysisDone();
        if (results == null || results.isEmpty()) return;

        System.out.println("==========================================================================================");
        System.out.printf("%-4s | %-12s | %-18s | %-6s | %-8s | %s%n",
                "RANK", "ACCOUNT ID", "USERNAME", "SCORE", "LEVEL", "RULES TRIGGERED");
        System.out.println("==========================================================================================");

        int rank = 1;
        for (AnalysisResult r : results) {
            String rules = r.getTriggeredRules().isEmpty() ? "None" : String.join(", ", r.getTriggeredRules());
            System.out.printf("%-4d | %-12s | @%-17s | %3d/100 | %-8s | %s%n",
                    rank++, r.getAccountId(), r.getUsername(), r.getRiskScore(), r.getRiskLevel().name(), rules);
        }
        System.out.println("==========================================================================================");
    }

    private void handleViewHighRiskAccounts() {
        List<AnalysisResult> results = ensureAnalysisDone();
        if (results == null || results.isEmpty()) return;

        List<AnalysisResult> highRisk = analysisService.getHighRiskAccounts();
        if (highRisk.isEmpty()) {
            System.out.println("[*] No accounts are currently flagged as HIGH RISK (score >= 60).");
            return;
        }

        System.out.println("================================================================================");
        System.out.printf("          HIGH-RISK ACCOUNTS DETECTED (%d ACCOUNT(S))%n", highRisk.size());
        System.out.println("================================================================================");

        for (AnalysisResult r : highRisk) {
            System.out.printf("Account: @%s (ID: %s)%n", r.getUsername(), r.getAccountId());
            System.out.printf("Risk Score: %d/100  |  Classification: %s%n", r.getRiskScore(), r.getRiskLevel().getDescription());
            System.out.println("Triggered Rules & Evidence:");
            for (int i = 0; i < r.getTriggeredRules().size(); i++) {
                String ruleName = r.getTriggeredRules().get(i);
                String explanation = (i < r.getExplanations().size()) ? r.getExplanations().get(i) : "";
                System.out.printf("  * [%s]: %s%n", ruleName, explanation);
            }
            System.out.println("--------------------------------------------------------------------------------");
        }
    }

    private void handleGenerateCsvReport() {
        List<AnalysisResult> results = ensureAnalysisDone();
        if (results == null || results.isEmpty()) return;

        try {
            File report = reportGenerator.generateCsvReport(results);
            System.out.printf("[✓] CSV Audit Report successfully generated at:%n    %s%n", report.getAbsolutePath());
        } catch (IOException e) {
            System.out.printf("[!] Error writing CSV report: %s%n", e.getMessage());
        }
    }

    private void handleGenerateTxtReport() {
        List<AnalysisResult> results = ensureAnalysisDone();
        if (results == null || results.isEmpty()) return;

        try {
            File report = reportGenerator.generateTxtReport(results);
            System.out.printf("[✓] TXT Audit Summary successfully generated at:%n    %s%n", report.getAbsolutePath());
        } catch (IOException e) {
            System.out.printf("[!] Error writing TXT report: %s%n", e.getMessage());
        }
    }

    private void handleViewDetectionRules() {
        List<DetectionRule> rules = analysisService.getActiveRules();
        System.out.println("================================================================================");
        System.out.println("               CONFIGURED DETECTION RULES & HEURISTICS                 ");
        System.out.println("================================================================================");
        System.out.printf("%-26s | %-7s | %s%n", "RULE NAME", "WEIGHT", "DESCRIPTION & LOGIC");
        System.out.println("--------------------------------------------------------------------------------");

        for (DetectionRule r : rules) {
            System.out.printf("%-26s | +%-5d | %s%n",
                    r.getRuleName(), r.getRiskPoints(), r.getGeneralDescription());
        }

        System.out.println("================================================================================");
        System.out.println("Scoring Bounds: 0 - 100 points maximum.");
        System.out.println("Risk Thresholds: 0-29: LOW  |  30-59: MEDIUM  |  60-100: HIGH");
        System.out.println("Ethical Principle: Scores are indicators to assist moderation, not absolute verdicts.");
        System.out.println("================================================================================");
    }

    private List<AnalysisResult> ensureAnalysisDone() {
        if (analysisService.getAccountCount() == 0) {
            System.out.println("[!] No accounts loaded. Please import CSV or add an account first.");
            return null;
        }

        List<AnalysisResult> results = analysisService.getLastAnalysisResults();
        if (results.isEmpty()) {
            System.out.println("[*] Accounts have not been analyzed yet. Running sequential analysis now...");
            return analysisService.analyzeAllSequential();
        }
        return results;
    }
}
