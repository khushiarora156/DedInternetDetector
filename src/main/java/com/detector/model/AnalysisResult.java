package com.detector.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Encapsulates the results of analyzing a single social media account.
 * Keeps analysis output and risk scoring separate from the raw account data.
 */
public class AnalysisResult implements Comparable<AnalysisResult> {
    private final String accountId;
    private final String username;
    private final int riskScore;
    private final RiskLevel riskLevel;
    private final List<String> triggeredRules;
    private final List<String> explanations;

    /**
     * Parameterized constructor for an AnalysisResult.
     *
     * @param accountId      Account ID analyzed
     * @param username       Social media handle
     * @param riskScore      Aggregated risk score capped between 0 and 100
     * @param riskLevel      Classification (LOW, MEDIUM, HIGH)
     * @param triggeredRules List of detection rule names that were triggered
     * @param explanations   Detailed explanations of why each rule fired
     */
    public AnalysisResult(String accountId, String username, int riskScore,
                          RiskLevel riskLevel, List<String> triggeredRules,
                          List<String> explanations) {
        this.accountId = Objects.requireNonNull(accountId, "accountId must not be null");
        this.username = Objects.requireNonNull(username, "username must not be null");
        this.riskScore = Math.max(0, Math.min(100, riskScore)); // Defensive clamp 0-100
        this.riskLevel = (riskLevel != null) ? riskLevel : RiskLevel.fromScore(this.riskScore);
        this.triggeredRules = (triggeredRules != null)
                ? new ArrayList<>(triggeredRules)
                : new ArrayList<>();
        this.explanations = (explanations != null)
                ? new ArrayList<>(explanations)
                : new ArrayList<>();
    }

    public String getAccountId() {
        return accountId;
    }

    public String getUsername() {
        return username;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public RiskLevel getRiskLevel() {
        return riskLevel;
    }

    public List<String> getTriggeredRules() {
        return Collections.unmodifiableList(triggeredRules);
    }

    public List<String> getExplanations() {
        return Collections.unmodifiableList(explanations);
    }

    public boolean isSuspicious() {
        return riskScore >= 30;
    }

    public boolean isHighRisk() {
        return riskLevel == RiskLevel.HIGH;
    }

    /**
     * Natural order sorts accounts by risk score in descending order (highest risk first).
     * In case of tie, breaks tie alphabetically by username.
     */
    @Override
    public int compareTo(AnalysisResult other) {
        int scoreCompare = Integer.compare(other.riskScore, this.riskScore);
        if (scoreCompare != 0) {
            return scoreCompare;
        }
        return this.username.compareToIgnoreCase(other.username);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AnalysisResult that = (AnalysisResult) o;
        return riskScore == that.riskScore &&
               Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId, riskScore);
    }

    @Override
    public String toString() {
        return String.format("[%s] @%-15s | Score: %3d/100 | Rules Triggered: %d",
                riskLevel, username, riskScore, triggeredRules.size());
    }
}
