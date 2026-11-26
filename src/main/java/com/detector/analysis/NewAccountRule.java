package com.detector.analysis;

import com.detector.model.SocialMediaAccount;

/**
 * Detection rule that flags freshly created accounts.
 * Disposable bot networks frequently spin up short-lived accounts to conduct spam campaigns.
 */
public class NewAccountRule implements DetectionRule {
    public static final int DEFAULT_THRESHOLD_DAYS = 30;
    public static final int DEFAULT_RISK_POINTS = 15;

    private final int thresholdDays;
    private final int riskPoints;

    public NewAccountRule() {
        this(DEFAULT_THRESHOLD_DAYS, DEFAULT_RISK_POINTS);
    }

    public NewAccountRule(int thresholdDays, int riskPoints) {
        if (thresholdDays < 0) {
            throw new IllegalArgumentException("Threshold days cannot be negative.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.thresholdDays = thresholdDays;
        this.riskPoints = riskPoints;
    }

    @Override
    public boolean isTriggered(SocialMediaAccount account) {
        if (account == null) {
            return false;
        }
        return account.getAccountAgeDays() < thresholdDays;
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return "NewAccountRule";
    }

    @Override
    public String getExplanation(SocialMediaAccount account) {
        if (account == null) {
            return "Account is null.";
        }
        return String.format(
            "Account age is %d day(s), which is under the %d-day threshold for established accounts.",
            account.getAccountAgeDays(), thresholdDays
        );
    }

    @Override
    public String getGeneralDescription() {
        return String.format(
            "Flags accounts younger than %d days (%d risk points).",
            thresholdDays, riskPoints
        );
    }

    public int getThresholdDays() {
        return thresholdDays;
    }
}
