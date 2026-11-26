package com.detector.analysis;

import com.detector.model.AnalysisResult;
import com.detector.model.RiskLevel;
import com.detector.model.SocialMediaAccount;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Executes a suite of DetectionRules against social media accounts to generate explainable AnalysisResults.
 * Allows dynamic rule registration, configuration, and inspection.
 */
public class RiskAnalyzer {
    private final List<DetectionRule> rules;
    private final RuleRegistry ruleRegistry;

    /**
     * Initializes RiskAnalyzer with the default 5 detection rules.
     */
    public RiskAnalyzer() {
        this.rules = new ArrayList<>();
        this.ruleRegistry = new RuleRegistry();
        registerDefaultRules();
    }

    /**
     * Initializes RiskAnalyzer with custom rules list.
     *
     * @param customRules custom detection rules to register
     */
    public RiskAnalyzer(List<DetectionRule> customRules) {
        this.rules = new ArrayList<>();
        this.ruleRegistry = new RuleRegistry();
        if (customRules != null) {
            for (DetectionRule r : customRules) {
                if (r != null) {
                    this.rules.add(r);
                    this.ruleRegistry.register(r);
                }
            }
        }
    }

    /**
     * Registers the default suite of 5 detection rules with baseline parameters.
     */
    public final void registerDefaultRules() {
        rules.clear();
        ruleRegistry.clear();
        DetectionRule[] defaults = new DetectionRule[] {
                new NewAccountRule(),
                new RepetitiveCommentRule(),
                new UnusualEngagementRule(),
                new RapidActivityRule(),
                new SuspiciousFollowRatioRule()
        };
        for (DetectionRule r : defaults) {
            rules.add(r);
            ruleRegistry.register(r);
        }
    }

    /**
     * Adds an additional or custom detection rule.
     *
     * @param rule DetectionRule to add
     */
    public void addRule(DetectionRule rule) {
        if (rule != null) {
            rules.add(rule);
            ruleRegistry.register(rule);
        }
    }

    /**
     * Returns the underlying RuleRegistry instance.
     *
     * @return rule registry
     */
    public RuleRegistry getRuleRegistry() {
        return ruleRegistry;
    }

    /**
     * Returns an unmodifiable list of currently active detection rules.
     *
     * @return List of DetectionRule
     */
    public List<DetectionRule> getRules() {
        return Collections.unmodifiableList(rules);
    }

    /**
     * Analyzes an individual social media account by evaluating each configured rule.
     * Generates a separate, immutable AnalysisResult.
     *
     * @param account account to analyze
     * @return AnalysisResult containing score, level, triggered rules, and explanations
     */
    public AnalysisResult analyze(SocialMediaAccount account) {
        if (account == null) {
            return new AnalysisResult("UNKNOWN", "unknown", 0, RiskLevel.LOW,
                    Collections.emptyList(), Collections.singletonList("Null account provided."));
        }

        List<DetectionRule> triggered = new ArrayList<>();
        List<String> triggeredRuleNames = new ArrayList<>();
        List<String> explanations = new ArrayList<>();

        for (DetectionRule rule : rules) {
            try {
                if (rule.isTriggered(account)) {
                    triggered.add(rule);
                    triggeredRuleNames.add(rule.getRuleName());
                    explanations.add(rule.getExplanation(account));
                }
            } catch (Exception e) {
                // Safeguard against any unexpected rule exception to prevent pipeline breakdown
                explanations.add(String.format("Rule '%s' evaluation skipped due to error: %s",
                        rule.getRuleName(), e.getMessage()));
            }
        }

        int score = RiskScoreCalculator.calculateScore(triggered);
        RiskLevel level = RiskScoreCalculator.determineRiskLevel(score);

        return new AnalysisResult(
                account.getAccountId(),
                account.getUsername(),
                score,
                level,
                triggeredRuleNames,
                explanations
        );
    }
}
