package com.detector.model;

/**
 * Represents the classified risk level of an analyzed account.
 * Classifications:
 * - LOW: 0 - 29 points (minimal or no suspicious indicators)
 * - MEDIUM: 30 - 59 points (moderate suspicious indicators, requires review)
 * - HIGH: 60 - 100 points (strong automated/bot patterns detected)
 */
public enum RiskLevel {
    LOW(0, 29, "LOW - Minimal Suspicion"),
    MEDIUM(30, 59, "MEDIUM - Moderate Suspicion"),
    HIGH(60, 100, "HIGH - Strong Bot Indicators");

    private final int minScore;
    private final int maxScore;
    private final String description;

    RiskLevel(int minScore, int maxScore, String description) {
        this.minScore = minScore;
        this.maxScore = maxScore;
        this.description = description;
    }

    public int getMinScore() {
        return minScore;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public String getDescription() {
        return description;
    }

    /**
     * Maps an integer risk score (0-100) to its corresponding RiskLevel.
     *
     * @param score the calculated risk score
     * @return RiskLevel (LOW, MEDIUM, or HIGH)
     */
    public static RiskLevel fromScore(int score) {
        if (score >= HIGH.minScore) {
            return HIGH;
        } else if (score >= MEDIUM.minScore) {
            return MEDIUM;
        } else {
            return LOW;
        }
    }
}
