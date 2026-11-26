package com.detector.analytics;

import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;

import java.util.Collection;

/**
 * Calculates aggregate statistical metrics from a collection of AnalysisResult objects.
 * Adheres strictly to the Single Responsibility Principle.
 */
public class RiskStatisticsCalculator {

    /**
     * Calculates a RiskDistributionSummary from analysis results.
     *
     * @param results collection of analysis results
     * @return calculated RiskDistributionSummary
     */
    public static RiskDistributionSummary calculateSummary(Collection<AnalysisResult> results) {
        if (results == null || results.isEmpty()) {
            return new RiskDistributionSummary(0, 0, 0, 0, 0.0, 0, 0);
        }

        int total = results.size();
        int lowCount = 0;
        int medCount = 0;
        int highCount = 0;
        int totalScore = 0;
        int minScore = Integer.MAX_VALUE;
        int maxScore = Integer.MIN_VALUE;

        for (AnalysisResult r : results) {
            int score = r.getRiskScore();
            totalScore += score;
            minScore = Math.min(minScore, score);
            maxScore = Math.max(maxScore, score);

            RiskLevel level = r.getRiskLevel();
            if (level == RiskLevel.HIGH) {
                highCount++;
            } else if (level == RiskLevel.MEDIUM) {
                medCount++;
            } else {
                lowCount++;
            }
        }

        double avgScore = (double) totalScore / total;
        return new RiskDistributionSummary(total, lowCount, medCount, highCount, avgScore, minScore, maxScore);
    }
}
