package com.detector.analysis;

import com.detector.model.RiskLevel;

import java.util.List;

/**
 * Calculates aggregate risk scores and classifies risk levels.
 * Guarantees that scores remain bounded within the range [0, 100].
 */
public class RiskScoreCalculator {
    public static final int MAX_SCORE = 100;
    public static final int MIN_SCORE = 0;

    /**
     * Calculates the total risk points from a list of triggered rules and caps the result at 100.
     *
     * @param triggeredRules list of triggered detection rules
     * @return capped risk score between 0 and 100
     */
    public static int calculateScore(List<DetectionRule> triggeredRules) {
        if (triggeredRules == null || triggeredRules.isEmpty()) {
            return MIN_SCORE;
        }

        int total = 0;
        for (DetectionRule rule : triggeredRules) {
            if (rule != null) {
                total += rule.getRiskPoints();
            }
        }

        return capScore(total);
    }

    /**
     * Clamps any raw score to the allowable range [0, 100].
     *
     * @param rawScore uncapped integer score
     * @return score between 0 and 100
     */
    public static int capScore(int rawScore) {
        if (rawScore < MIN_SCORE) {
            return MIN_SCORE;
        }
        return Math.min(rawScore, MAX_SCORE);
    }

    /**
     * Maps an integer score to the corresponding RiskLevel.
     * 0 - 29   -> LOW
     * 30 - 59  -> MEDIUM
     * 60 - 100 -> HIGH
     *
     * @param score risk score (0-100)
     * @return RiskLevel classification
     */
    public static RiskLevel determineRiskLevel(int score) {
        return RiskLevel.fromScore(score);
    }
}
