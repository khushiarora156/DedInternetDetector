package com.detector.analysis;

import com.detector.model.SocialMediaAccount;

/**
 * Strategy interface defining detection rules for identifying automated or suspicious social media accounts.
 * Demonstrates the Strategy pattern, polymorphism, and abstraction.
 */
public interface DetectionRule {

    /**
     * Evaluates whether the given account triggers this detection rule.
     *
     * @param account account to evaluate
     * @return true if the rule conditions are met; false otherwise
     */
    boolean isTriggered(SocialMediaAccount account);

    /**
     * Returns the risk points assigned if this rule is triggered.
     *
     * @return risk points (positive integer)
     */
    int getRiskPoints();

    /**
     * Returns the human-readable identifier/name of the rule.
     *
     * @return rule name
     */
    String getRuleName();

    /**
     * Generates a context-specific explanation of why the rule was triggered for this account.
     *
     * @param account evaluated account
     * @return explanatory string detailing the metrics that caused the flag
     */
    String getExplanation(SocialMediaAccount account);

    /**
     * Returns a general description of what this rule inspects.
     *
     * @return general rule description
     */
    String getGeneralDescription();
}
