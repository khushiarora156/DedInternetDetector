package com.detector.analysis;

import com.detector.model.SocialMediaAccount;

/**
 * Detection rule that flags accounts with disproportionate follow-to-follower ratios (mass following).
 * Spammer bots often mass-follow thousands of users hoping for follow-backs while accumulating few followers.
 * Accounts with very few followers (e.g. new or private personal accounts) are protected by a minimum following threshold.
 */
public class SuspiciousFollowRatioRule implements DetectionRule {
    public static final double DEFAULT_THRESHOLD_RATIO = 10.0; // 10x more following than followers
    public static final int DEFAULT_MIN_FOLLOWING = 150;        // Guard against false positives on small accounts
    public static final int DEFAULT_RISK_POINTS = 15;

    private final double thresholdRatio;
    private final int minFollowing;
    private final int riskPoints;

    public SuspiciousFollowRatioRule() {
        this(DEFAULT_THRESHOLD_RATIO, DEFAULT_MIN_FOLLOWING, DEFAULT_RISK_POINTS);
    }

    public SuspiciousFollowRatioRule(double thresholdRatio, int minFollowing, int riskPoints) {
        if (thresholdRatio <= 0.0) {
            throw new IllegalArgumentException("Threshold ratio must be greater than 0.");
        }
        if (minFollowing < 0) {
            throw new IllegalArgumentException("Minimum following cannot be negative.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.thresholdRatio = thresholdRatio;
        this.minFollowing = minFollowing;
        this.riskPoints = riskPoints;
    }

    @Override
    public boolean isTriggered(SocialMediaAccount account) {
        if (account == null) {
            return false;
        }

        int following = account.getFollowing();
        int followers = account.getFollowers();

        // Guardrail: Avoid flagging small or personal accounts that just started following people
        if (following < minFollowing) {
            return false;
        }

        double ratio = (followers > 0) ? ((double) following / followers) : (double) following;
        return ratio >= thresholdRatio;
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return "SuspiciousFollowRatioRule";
    }

    @Override
    public String getExplanation(SocialMediaAccount account) {
        if (account == null) {
            return "Account is null.";
        }
        int following = account.getFollowing();
        int followers = account.getFollowers();
        double ratio = (followers > 0) ? ((double) following / followers) : (double) following;

        return String.format(
            "Account follows %d accounts while having only %d followers (ratio: %.1fx), exceeding threshold of %.1fx (min following: %d).",
            following, followers, ratio, thresholdRatio, minFollowing
        );
    }

    @Override
    public String getGeneralDescription() {
        return String.format(
            "Flags accounts following >= %d accounts with following-to-follower ratio >= %.1fx (%d risk points).",
            minFollowing, thresholdRatio, riskPoints
        );
    }

    public double getThresholdRatio() {
        return thresholdRatio;
    }

    public int getMinFollowing() {
        return minFollowing;
    }
}
