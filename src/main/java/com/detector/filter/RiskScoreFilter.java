package com.detector.filter;

import com.detector.model.AnalysisResult;

import java.util.function.Predicate;

/**
 * Predicate filter for AnalysisResult based on risk score bounds.
 */
public class RiskScoreFilter implements Predicate<AnalysisResult> {
    private final int minScore;
    private final int maxScore;

    public RiskScoreFilter(int minScore, int maxScore) {
        if (minScore > maxScore) {
            throw new IllegalArgumentException("minScore cannot be greater than maxScore.");
        }
        this.minScore = minScore;
        this.maxScore = maxScore;
    }

    public static RiskScoreFilter atLeast(int minScore) {
        return new RiskScoreFilter(minScore, 100);
    }

    public static RiskScoreFilter atMost(int maxScore) {
        return new RiskScoreFilter(0, maxScore);
    }

    public static RiskScoreFilter between(int minScore, int maxScore) {
        return new RiskScoreFilter(minScore, maxScore);
    }

    @Override
    public boolean test(AnalysisResult result) {
        if (result == null) {
            return false;
        }
        int score = result.getRiskScore();
        return score >= minScore && score <= maxScore;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }
}
