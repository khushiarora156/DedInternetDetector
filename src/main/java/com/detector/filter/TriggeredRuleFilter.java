package com.detector.filter;

import com.detector.model.AnalysisResult;

import java.util.Objects;
import java.util.function.Predicate;

/**
 * Predicate filter for AnalysisResult matching accounts that triggered a specific detection rule.
 */
public class TriggeredRuleFilter implements Predicate<AnalysisResult> {
    private final String targetRuleName;

    public TriggeredRuleFilter(String targetRuleName) {
        this.targetRuleName = Objects.requireNonNull(targetRuleName, "targetRuleName must not be null").trim();
    }

    @Override
    public boolean test(AnalysisResult result) {
        if (result == null || result.getTriggeredRules() == null) {
            return false;
        }
        for (String rule : result.getTriggeredRules()) {
            if (rule.equalsIgnoreCase(targetRuleName)) {
                return true;
            }
        }
        return false;
    }

    public String getTargetRuleName() {
        return targetRuleName;
    }
}
