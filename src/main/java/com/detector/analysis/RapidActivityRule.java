package com.detector.analysis;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;

/**
 * Detection rule that flags high-frequency automated commenting bursts within a short timeframe.
 * Human users rarely sustain more than 20 distinct comments across threads in a 10-minute span.
 */
public class RapidActivityRule implements DetectionRule {
    public static final int DEFAULT_THRESHOLD_BURST_COMMENTS = 20;
    public static final int DEFAULT_RISK_POINTS = 15;

    private final int thresholdBurstComments;
    private final int riskPoints;

    public RapidActivityRule() {
        this(DEFAULT_THRESHOLD_BURST_COMMENTS, DEFAULT_RISK_POINTS);
    }

    public RapidActivityRule(int thresholdBurstComments, int riskPoints) {
        if (thresholdBurstComments < 0) {
            throw new IllegalArgumentException("Threshold burst comments cannot be negative.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.thresholdBurstComments = thresholdBurstComments;
        this.riskPoints = riskPoints;
    }

    @Override
    public boolean isTriggered(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return false;
        }
        AccountActivity act = account.getActivity();
        return act.getCommentsInTenMinutes() > thresholdBurstComments;
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return "RapidActivityRule";
    }

    @Override
    public String getExplanation(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return "Activity data unavailable.";
        }
        AccountActivity act = account.getActivity();
        return String.format(
            "Account logged %d comments within a 10-minute window, exceeding human velocity threshold of %d comments.",
            act.getCommentsInTenMinutes(), thresholdBurstComments
        );
    }

    @Override
    public String getGeneralDescription() {
        return String.format(
            "Flags burst activity exceeding %d comments in a 10-minute window (%d risk points).",
            thresholdBurstComments, riskPoints
        );
    }

    public int getThresholdBurstComments() {
        return thresholdBurstComments;
    }
}
