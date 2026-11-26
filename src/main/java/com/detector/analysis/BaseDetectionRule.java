package com.detector.analysis;

import com.detector.model.SocialMediaAccount;

/**
 * Abstract base class for detection rules providing common attributes and default accessors.
 * Adheres to the Strategy and Template Method design patterns.
 */
public abstract class BaseDetectionRule implements DetectionRule {
    protected final String ruleName;
    protected final int riskPoints;
    protected final String generalDescription;

    /**
     * Protected constructor for subclasses.
     *
     * @param ruleName           unique identifier for the rule
     * @param riskPoints         risk score points attributed when triggered (>= 0)
     * @param generalDescription human-readable explanation of what the rule detects
     */
    protected BaseDetectionRule(String ruleName, int riskPoints, String generalDescription) {
        if (ruleName == null || ruleName.trim().isEmpty()) {
            throw new IllegalArgumentException("Rule name cannot be null or empty.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.ruleName = ruleName.trim();
        this.riskPoints = riskPoints;
        this.generalDescription = (generalDescription != null) ? generalDescription : "";
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return ruleName;
    }

    @Override
    public String getGeneralDescription() {
        return generalDescription;
    }

    @Override
    public String toString() {
        return String.format("%s [points=%d]", ruleName, riskPoints);
    }
}
