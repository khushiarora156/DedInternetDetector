package com.detector.filter;

import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Predicate filter for AnalysisResult matching a target RiskLevel.
 */
public class RiskLevelFilter implements Predicate<AnalysisResult> {
    private final RiskLevel targetLevel;

    public RiskLevelFilter(RiskLevel targetLevel) {
        this.targetLevel = Objects.requireNonNull(targetLevel, "targetLevel must not be null");
    }

    @Override
    public boolean test(AnalysisResult result) {
        if (result == null) {
            return false;
        }
        return result.getRiskLevel() == targetLevel;
    }

    public RiskLevel getTargetLevel() {
        return targetLevel;
    }
}
