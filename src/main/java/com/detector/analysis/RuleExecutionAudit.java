package com.detector.analysis;

import java.util.Objects;

/**
 * Tracks execution statistics and metrics for a specific detection rule during analysis.
 * Facilitates audit logging, transparency, and heuristic explainability.
 */
public class RuleExecutionAudit {
    private final String ruleName;
    private int evaluationCount;
    private int triggerCount;
    private int totalRiskPointsContributed;

    public RuleExecutionAudit(String ruleName) {
        this.ruleName = Objects.requireNonNull(ruleName, "Rule name must not be null").trim();
        this.evaluationCount = 0;
        this.triggerCount = 0;
        this.totalRiskPointsContributed = 0;
    }

    /**
     * Records an evaluation of the rule.
     *
     * @param triggered whether the rule was triggered
     * @param points    points contributed if triggered
     */
    public synchronized void recordEvaluation(boolean triggered, int points) {
        evaluationCount++;
        if (triggered) {
            triggerCount++;
            totalRiskPointsContributed += Math.max(0, points);
        }
    }

    public String getRuleName() {
        return ruleName;
    }

    public synchronized int getEvaluationCount() {
        return evaluationCount;
    }

    public synchronized int getTriggerCount() {
        return triggerCount;
    }

    public synchronized int getTotalRiskPointsContributed() {
        return totalRiskPointsContributed;
    }

    /**
     * Calculates the trigger rate percentage (triggers / evaluations * 100).
     *
     * @return percentage between 0.0 and 100.0
     */
    public synchronized double getTriggerRate() {
        if (evaluationCount == 0) {
            return 0.0;
        }
        return ((double) triggerCount / evaluationCount) * 100.0;
    }

    @Override
    public synchronized String toString() {
        return String.format("RuleAudit[%s: evaluations=%d, triggers=%d (%.1f%%), points=%d]",
                ruleName, evaluationCount, triggerCount, getTriggerRate(), totalRiskPointsContributed);
    }
}
