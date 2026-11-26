package com.detector.analysis;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;

/**
 * Detection rule that flags statistically improbable engagement rates.
 * While organic engagement rates typically sit between 1% and 5%, bot farms and
 * engagement pods often generate exaggerated like/comment volumes.
 */
public class UnusualEngagementRule implements DetectionRule {
    public static final double DEFAULT_THRESHOLD_PERCENT = 25.0; // 25% engagement rate
    public static final int DEFAULT_RISK_POINTS = 15;

    private final double thresholdPercent;
    private final int riskPoints;

    public UnusualEngagementRule() {
        this(DEFAULT_THRESHOLD_PERCENT, DEFAULT_RISK_POINTS);
    }

    public UnusualEngagementRule(double thresholdPercent, int riskPoints) {
        if (thresholdPercent < 0) {
            throw new IllegalArgumentException("Threshold percent cannot be negative.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.thresholdPercent = thresholdPercent;
        this.riskPoints = riskPoints;
    }

    @Override
    public boolean isTriggered(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return false;
        }

        int followers = account.getFollowers();
        AccountActivity act = account.getActivity();
        double totalInteractions = act.getAverageLikes() + act.getAverageComments();

        // Safe handling for zero followers:
        // If an account has 0 followers but records post interactions, it's synthetic activity.
        // If it has 0 followers and 0 interactions, it's simply inactive/new (not suspicious).
        if (followers <= 0) {
            return totalInteractions > 0;
        }

        // Standard engagement rate calculation: ((likes + comments) / followers) * 100
        double engagementRate = (totalInteractions / followers) * 100.0;
        return engagementRate >= thresholdPercent;
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return "UnusualEngagementRule";
    }

    @Override
    public String getExplanation(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return "Activity data unavailable.";
        }
        int followers = account.getFollowers();
        AccountActivity act = account.getActivity();
        double totalInteractions = act.getAverageLikes() + act.getAverageComments();

        if (followers <= 0) {
            return String.format(
                "Account has 0 followers yet averages %.1f interactions (likes/comments), indicating synthetic engagement.",
                totalInteractions
            );
        }

        double rate = (totalInteractions / followers) * 100.0;
        return String.format(
            "Engagement rate is %.2f%% (%.1f avg interactions / %d followers), exceeding threshold of %.1f%%.",
            rate, totalInteractions, followers, thresholdPercent
        );
    }

    @Override
    public String getGeneralDescription() {
        return String.format(
            "Flags accounts with engagement rate exceeding %.1f%% or interactions with zero followers (%d risk points).",
            thresholdPercent, riskPoints
        );
    }

    public double getThresholdPercent() {
        return thresholdPercent;
    }
}
