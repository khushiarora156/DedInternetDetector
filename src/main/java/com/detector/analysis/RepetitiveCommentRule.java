package com.detector.analysis;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;

/**
 * Detection rule that flags accounts posting high ratios of duplicate or templated comments.
 * Bot swarms typically reuse scripted comments across threads.
 */
public class RepetitiveCommentRule implements DetectionRule {
    public static final double DEFAULT_THRESHOLD_RATIO = 0.50; // 50%
    public static final int DEFAULT_MIN_SAMPLE_SIZE = 5;
    public static final int DEFAULT_RISK_POINTS = 20;

    private final double thresholdRatio;
    private final int minSampleSize;
    private final int riskPoints;

    public RepetitiveCommentRule() {
        this(DEFAULT_THRESHOLD_RATIO, DEFAULT_MIN_SAMPLE_SIZE, DEFAULT_RISK_POINTS);
    }

    public RepetitiveCommentRule(double thresholdRatio, int minSampleSize, int riskPoints) {
        if (thresholdRatio < 0.0 || thresholdRatio > 1.0) {
            throw new IllegalArgumentException("Threshold ratio must be between 0.0 and 1.0.");
        }
        if (minSampleSize < 1) {
            throw new IllegalArgumentException("Minimum sample size must be at least 1.");
        }
        if (riskPoints < 0) {
            throw new IllegalArgumentException("Risk points cannot be negative.");
        }
        this.thresholdRatio = thresholdRatio;
        this.minSampleSize = minSampleSize;
        this.riskPoints = riskPoints;
    }

    @Override
    public boolean isTriggered(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return false;
        }
        AccountActivity act = account.getActivity();
        // Require sufficient sample size to avoid false positives on sparse data
        if (act.getTotalCommentsObserved() < minSampleSize) {
            return false;
        }

        double ratio = act.getRepetitiveCommentRatio();
        return ratio >= thresholdRatio;
    }

    @Override
    public int getRiskPoints() {
        return riskPoints;
    }

    @Override
    public String getRuleName() {
        return "RepetitiveCommentRule";
    }

    @Override
    public String getExplanation(SocialMediaAccount account) {
        if (account == null || account.getActivity() == null) {
            return "Activity data unavailable.";
        }
        AccountActivity act = account.getActivity();
        double ratio = act.getRepetitiveCommentRatio() * 100.0;
        return String.format(
            "Repetitive comments ratio is %.1f%% (%d of %d comments repeated), exceeding threshold of %.1f%% (min sample %d).",
            ratio, act.getRepeatedComments(), act.getTotalCommentsObserved(), thresholdRatio * 100.0, minSampleSize
        );
    }

    @Override
    public String getGeneralDescription() {
        return String.format(
            "Flags accounts where repeated comments constitute >= %.0f%% of total comments (min sample %d comments, %d risk points).",
            thresholdRatio * 100.0, minSampleSize, riskPoints
        );
    }

    public double getThresholdRatio() {
        return thresholdRatio;
    }

    public int getMinSampleSize() {
        return minSampleSize;
    }
}
