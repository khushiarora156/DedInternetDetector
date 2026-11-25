package com.detector.analytics;

/**
 * Immutable value object holding statistical distribution metrics for an analysis batch.
 * Replaces scattered manual count loops with a unified, encapsulated model.
 */
public class RiskDistributionSummary {
    private final int totalAccounts;
    private final int lowRiskCount;
    private final int mediumRiskCount;
    private final int highRiskCount;
    private final double averageScore;
    private final int minScore;
    private final int maxScore;

    public RiskDistributionSummary(int totalAccounts, int lowRiskCount, int mediumRiskCount,
                                   int highRiskCount, double averageScore, int minScore, int maxScore) {
        this.totalAccounts = Math.max(0, totalAccounts);
        this.lowRiskCount = Math.max(0, lowRiskCount);
        this.mediumRiskCount = Math.max(0, mediumRiskCount);
        this.highRiskCount = Math.max(0, highRiskCount);
        this.averageScore = averageScore;
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public int getTotalAccounts() {
        return totalAccounts;
    }

    public int getLowRiskCount() {
        return lowRiskCount;
    }

    public int getMediumRiskCount() {
        return mediumRiskCount;
    }

    public int getHighRiskCount() {
        return highRiskCount;
    }

    public double getLowRiskPercentage() {
        return totalAccounts > 0 ? (lowRiskCount * 100.0 / totalAccounts) : 0.0;
    }

    public double getMediumRiskPercentage() {
        return totalAccounts > 0 ? (mediumRiskCount * 100.0 / totalAccounts) : 0.0;
    }

    public double getHighRiskPercentage() {
        return totalAccounts > 0 ? (highRiskCount * 100.0 / totalAccounts) : 0.0;
    }

    public double getAverageScore() {
        return averageScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public int getSuspiciousCount() {
        return mediumRiskCount + highRiskCount;
    }

    public double getSuspiciousPercentage() {
        return totalAccounts > 0 ? (getSuspiciousCount() * 100.0 / totalAccounts) : 0.0;
    }

    @Override
    public String toString() {
        return String.format("Summary[Total=%d, Low=%d (%.1f%%), Med=%d (%.1f%%), High=%d (%.1f%%), Avg=%.1f]",
                totalAccounts, lowRiskCount, getLowRiskPercentage(),
                mediumRiskCount, getMediumRiskPercentage(),
                highRiskCount, getHighRiskPercentage(), averageScore);
    }
}
