package com.detector.report;

import com.detector.analytics.RiskDistributionSummary;
import com.detector.analytics.RiskStatisticsCalculator;
import com.detector.model.AnalysisResult;

import java.util.List;

/**
 * Formats comprehensive human-readable text audit reports.
 * Decouples textual presentation from file writing operations.
 */
public class TxtReportFormatter {

    /**
     * Formats a list of AnalysisResults and metadata into an audit report.
     *
     * @param results  analysis results
     * @param metadata report metadata
     * @return formatted plain text report
     */
    public String format(List<AnalysisResult> results, ReportMetadata metadata) {
        RiskDistributionSummary summary = RiskStatisticsCalculator.calculateSummary(results);
        StringBuilder sb = new StringBuilder();

        sb.append("================================================================================").append(System.lineSeparator());
        sb.append("         DEAD INTERNET & FAKE ENGAGEMENT DETECTOR - AUDIT REPORT        ").append(System.lineSeparator());
        sb.append("================================================================================").append(System.lineSeparator());
        sb.append(String.format("Generated At      : %s%n", metadata.getFormattedDisplayTime()));
        sb.append(String.format("Accounts Analyzed : %d%n", summary.getTotalAccounts()));
        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        sb.append("1. RISK DISTRIBUTION SUMMARY").append(System.lineSeparator());
        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("  [HIGH RISK]   (60 - 100 pts) : %3d accounts (%.1f%%)%n",
                summary.getHighRiskCount(), summary.getHighRiskPercentage()));
        sb.append(String.format("  [MEDIUM RISK] (30 - 59 pts)  : %3d accounts (%.1f%%)%n",
                summary.getMediumRiskCount(), summary.getMediumRiskPercentage()));
        sb.append(String.format("  [LOW RISK]    (00 - 29 pts)  : %3d accounts (%.1f%%)%n",
                summary.getLowRiskCount(), summary.getLowRiskPercentage()));
        sb.append(System.lineSeparator());

        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        sb.append("2. RANKED ACCOUNTS (BY RISK SCORE - DESCENDING)").append(System.lineSeparator());
        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        sb.append(String.format("%-4s | %-12s | %-18s | %-6s | %-8s | %s%n",
                "RANK", "ACCOUNT ID", "USERNAME", "SCORE", "LEVEL", "RULES TRIGGERED"));
        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());

        if (results != null) {
            int rank = 1;
            for (AnalysisResult r : results) {
                String rules = r.getTriggeredRules().isEmpty() ? "None" : String.join(", ", r.getTriggeredRules());
                sb.append(String.format("%-4d | %-12s | @%-17s | %3d/100 | %-8s | %s%n",
                        rank++, r.getAccountId(), r.getUsername(), r.getRiskScore(), r.getRiskLevel().name(), rules));
            }
        }
        sb.append(System.lineSeparator());

        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        sb.append("3. DETAILED ACCOUNT BREAKDOWN & RULE EXPLANATIONS").append(System.lineSeparator());
        sb.append("--------------------------------------------------------------------------------").append(System.lineSeparator());
        if (results != null) {
            for (AnalysisResult r : results) {
                sb.append(String.format("Account: @%s (ID: %s)%n", r.getUsername(), r.getAccountId()));
                sb.append(String.format("Risk Score: %d/100  |  Classification: %s%n", r.getRiskScore(), r.getRiskLevel().getDescription()));
                if (r.getTriggeredRules().isEmpty()) {
                    sb.append("  -> No suspicious indicators were triggered. Behavior appears consistent with authentic usage.").append(System.lineSeparator());
                } else {
                    sb.append("  -> Triggered Indicators:").append(System.lineSeparator());
                    for (int i = 0; i < r.getTriggeredRules().size(); i++) {
                        String ruleName = r.getTriggeredRules().get(i);
                        String explanation = (i < r.getExplanations().size()) ? r.getExplanations().get(i) : "";
                        sb.append(String.format("     * [%s]: %s%n", ruleName, explanation));
                    }
                }
                sb.append(System.lineSeparator());
            }
        }

        sb.append("================================================================================").append(System.lineSeparator());
        sb.append("IMPORTANT ETHICAL NOTICE & DISCLAIMER:").append(System.lineSeparator());
        sb.append("This risk score is an indicator derived from statistical heuristics and pattern").append(System.lineSeparator());
        sb.append("detection rules. It is designed to assist human moderation and audit workflows.").append(System.lineSeparator());
        sb.append("The application does NOT claim that an account is definitively fake or automated.").append(System.lineSeparator());
        sb.append("================================================================================").append(System.lineSeparator());

        return sb.toString();
    }
}
