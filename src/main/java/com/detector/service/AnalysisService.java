package com.detector.service;

import com.detector.analysis.DetectionRule;
import com.detector.analysis.RiskAnalyzer;
import com.detector.manager.AccountManager;
import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;
import com.detector.model.SocialMediaAccount;
import com.detector.validation.DataValidator;
import com.detector.validation.ValidationResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * High-level coordinator service orchestrating account registration, validation,
 * sequential and multithreaded detection, sorting, and filtering.
 * Demonstrates Java concurrency (ExecutorService, Callable, Future) and Clean Architecture.
 */
public class AnalysisService {
    private final AccountManager accountManager;
    private final RiskAnalyzer riskAnalyzer;
    private final List<AnalysisResult> lastAnalysisResults;
    private long lastAnalysisDurationMs;
    private boolean lastAnalysisWasParallel;

    public AnalysisService() {
        this(new AccountManager(), new RiskAnalyzer());
    }

    public AnalysisService(AccountManager accountManager, RiskAnalyzer riskAnalyzer) {
        this.accountManager = (accountManager != null) ? accountManager : new AccountManager();
        this.riskAnalyzer = (riskAnalyzer != null) ? riskAnalyzer : new RiskAnalyzer();
        this.lastAnalysisResults = new ArrayList<>();
        this.lastAnalysisDurationMs = 0;
        this.lastAnalysisWasParallel = false;
    }

    public AccountManager getAccountManager() {
        return accountManager;
    }

    public RiskAnalyzer getRiskAnalyzer() {
        return riskAnalyzer;
    }

    public List<DetectionRule> getActiveRules() {
        return riskAnalyzer.getRules();
    }

    /**
     * Validates and registers an account.
     *
     * @param account account to register
     * @return ValidationResult indicating success or validation errors
     */
    public ValidationResult registerAccount(SocialMediaAccount account) {
        ValidationResult validation = DataValidator.validateAccount(account);
        if (!validation.isValid()) {
            return validation;
        }

        if (accountManager.accountExists(account.getAccountId())) {
            return ValidationResult.failure("Duplicate Account ID: '" + account.getAccountId() + "' already exists.");
        }

        boolean added = accountManager.addAccount(account);
        if (!added) {
            return ValidationResult.failure("Failed to add account into management store.");
        }

        return ValidationResult.success();
    }

    /**
     * Executes sequential (single-threaded) analysis across all registered accounts.
     *
     * @return List of AnalysisResults sorted descending by risk score
     */
    public synchronized List<AnalysisResult> analyzeAllSequential() {
        List<SocialMediaAccount> accounts = accountManager.getAllAccounts();
        long startTime = System.currentTimeMillis();

        List<AnalysisResult> results = new ArrayList<>();
        for (SocialMediaAccount acc : accounts) {
            results.add(riskAnalyzer.analyze(acc));
        }

        // Sort descending by risk score
        Collections.sort(results);

        this.lastAnalysisDurationMs = System.currentTimeMillis() - startTime;
        this.lastAnalysisWasParallel = false;
        this.lastAnalysisResults.clear();
        this.lastAnalysisResults.addAll(results);

        return Collections.unmodifiableList(results);
    }

    /**
     * Executes parallel (multithreaded) analysis across all registered accounts.
     * Uses Java ExecutorService and Callable to distribute account evaluations across threads.
     *
     * @param threadPoolSize number of worker threads (default e.g. Runtime.getRuntime().availableProcessors())
     * @return List of AnalysisResults sorted descending by risk score
     */
    public synchronized List<AnalysisResult> analyzeAllParallel(int threadPoolSize) {
        List<SocialMediaAccount> accounts = accountManager.getAllAccounts();
        if (accounts.isEmpty()) {
            this.lastAnalysisDurationMs = 0;
            this.lastAnalysisWasParallel = true;
            this.lastAnalysisResults.clear();
            return Collections.emptyList();
        }

        int threads = Math.max(1, Math.min(threadPoolSize, accounts.size()));
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        long startTime = System.currentTimeMillis();

        List<AnalysisResult> results = new ArrayList<>();
        try {
            // Create Callable tasks for each account
            List<Callable<AnalysisResult>> tasks = new ArrayList<>();
            for (SocialMediaAccount acc : accounts) {
                tasks.add(() -> riskAnalyzer.analyze(acc));
            }

            // Submit all tasks and wait for completion
            List<Future<AnalysisResult>> futures = executor.invokeAll(tasks);
            for (Future<AnalysisResult> future : futures) {
                try {
                    results.add(future.get());
                } catch (ExecutionException e) {
                    System.err.println("Error analyzing account in worker thread: " + e.getCause().getMessage());
                }
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.err.println("Parallel analysis was interrupted: " + e.getMessage());
        } finally {
            // Proper orderly shutdown of the ExecutorService
            executor.shutdown();
            try {
                if (!executor.awaitTermination(5, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }

        // Sort descending by risk score (guarantees identical ordering to sequential)
        Collections.sort(results);

        this.lastAnalysisDurationMs = System.currentTimeMillis() - startTime;
        this.lastAnalysisWasParallel = true;
        this.lastAnalysisResults.clear();
        this.lastAnalysisResults.addAll(results);

        return Collections.unmodifiableList(results);
    }

    /**
     * Returns cached results from the last analysis run.
     */
    public synchronized List<AnalysisResult> getLastAnalysisResults() {
        return Collections.unmodifiableList(lastAnalysisResults);
    }

    /**
     * Filters the latest analysis results returning only those marked as HIGH risk.
     *
     * @return List of high-risk AnalysisResults
     */
    public synchronized List<AnalysisResult> getHighRiskAccounts() {
        return lastAnalysisResults.stream()
                .filter(r -> r.getRiskLevel() == RiskLevel.HIGH)
                .collect(Collectors.toList());
    }

    /**
     * Returns an AnalysisResult for a specific account ID if present in the last analysis run.
     *
     * @param accountId ID to locate
     * @return Optional containing AnalysisResult if found
     */
    public synchronized Optional<AnalysisResult> getResultForAccount(String accountId) {
        if (accountId == null) return Optional.empty();
        return lastAnalysisResults.stream()
                .filter(r -> r.getAccountId().equalsIgnoreCase(accountId.trim()))
                .findFirst();
    }

    public long getLastAnalysisDurationMs() {
        return lastAnalysisDurationMs;
    }

    public boolean wasLastAnalysisParallel() {
        return lastAnalysisWasParallel;
    }

    public int getAccountCount() {
        return accountManager.getAccountCount();
    }

    /**
     * Filters the latest analysis results using a Predicate filter (e.g. RiskScoreFilter, RiskLevelFilter).
     *
     * @param filter predicate filter
     * @return filtered list of AnalysisResults
     */
    public synchronized List<AnalysisResult> filterResults(java.util.function.Predicate<AnalysisResult> filter) {
        if (filter == null) {
            return Collections.unmodifiableList(new ArrayList<>(lastAnalysisResults));
        }
        return lastAnalysisResults.stream().filter(filter).collect(Collectors.toList());
    }

    /**
     * Runs both sequential and parallel analysis to generate comparative benchmark telemetry.
     *
     * @param threadPoolSize worker thread count
     * @return BenchmarkResult containing comparative timings
     */
    public synchronized BenchmarkResult runBenchmark(int threadPoolSize) {
        long seqStart = System.currentTimeMillis();
        analyzeAllSequential();
        long seqDuration = System.currentTimeMillis() - seqStart;

        long parStart = System.currentTimeMillis();
        analyzeAllParallel(threadPoolSize);
        long parDuration = System.currentTimeMillis() - parStart;

        return new BenchmarkResult(getAccountCount(), threadPoolSize, seqDuration, parDuration);
    }
}
