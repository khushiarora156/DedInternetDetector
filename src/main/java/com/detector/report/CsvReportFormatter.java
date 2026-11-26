package com.detector.report;

import com.detector.model.AnalysisResult;

import java.util.List;

/**
 * Formats analysis results into RFC 4180 compliant CSV representations.
 * Adheres to Single Responsibility by separating formatting from disk I/O.
 */
public class CsvReportFormatter {
    public static final String CSV_HEADER = "AccountId,Username,RiskScore,RiskLevel,RulesTriggeredCount,TriggeredRules,Explanations";

    /**
     * Formats a list of analysis results into a complete CSV string.
     *
     * @param results analysis results to format
     * @return formatted CSV content
     */
    public String format(List<AnalysisResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append(CSV_HEADER).append(System.lineSeparator());

        if (results != null) {
            for (AnalysisResult r : results) {
                sb.append(formatRow(r)).append(System.lineSeparator());
            }
        }

        return sb.toString();
    }

    /**
     * Formats an individual AnalysisResult into a CSV row.
     */
    public String formatRow(AnalysisResult r) {
        String rules = String.join(" | ", r.getTriggeredRules());
        String explanations = String.join(" | ", r.getExplanations()).replace("\"", "\"\"");

        return String.format("\"%s\",\"%s\",%d,\"%s\",%d,\"%s\",\"%s\"",
                escapeCsv(r.getAccountId()),
                escapeCsv(r.getUsername()),
                r.getRiskScore(),
                r.getRiskLevel().name(),
                r.getTriggeredRules().size(),
                escapeCsv(rules),
                explanations
        );
    }

    private String escapeCsv(String input) {
        if (input == null) return "";
        return input.replace("\"", "\"\"");
    }
}
