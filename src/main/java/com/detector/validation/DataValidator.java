package com.detector.validation;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates account and activity attributes before registration or analysis.
 * Ensures consistent business integrity across both CSV import and manual console input.
 */
public class DataValidator {

    /**
     * Comprehensive validation of a SocialMediaAccount object.
     *
     * @param account account to validate
     * @return ValidationResult containing status and any error messages
     */
    public static ValidationResult validateAccount(SocialMediaAccount account) {
        if (account == null) {
            return ValidationResult.failure("Account object is null.");
        }

        List<String> errors = new ArrayList<>();
        List<String> warnings = new ArrayList<>();

        // Validate account ID
        if (account.getAccountId() == null || account.getAccountId().trim().isEmpty()) {
            errors.add("Account ID cannot be blank.");
        } else if (!account.getAccountId().matches("^[a-zA-Z0-9_-]{2,30}$")) {
            errors.add("Account ID must be 2-30 alphanumeric characters, hyphens, or underscores.");
        }

        // Validate username
        if (account.getUsername() == null || account.getUsername().trim().isEmpty()) {
            errors.add("Username cannot be blank.");
        } else if (!account.getUsername().matches("^[a-zA-Z0-9_.-]{2,30}$")) {
            errors.add("Username must be 2-30 characters (letters, numbers, dot, underscore, dash).");
        }

        // Validate follower/following counts
        if (account.getFollowers() < 0) {
            errors.add("Follower count cannot be negative: " + account.getFollowers());
        }
        if (account.getFollowing() < 0) {
            errors.add("Following count cannot be negative: " + account.getFollowing());
        }

        // Validate posts and age
        if (account.getTotalPosts() < 0) {
            errors.add("Total posts cannot be negative: " + account.getTotalPosts());
        }
        if (account.getAccountAgeDays() < 0) {
            errors.add("Account age cannot be negative: " + account.getAccountAgeDays());
        }

        // Validate activity
        AccountActivity activity = account.getActivity();
        if (activity == null) {
            errors.add("Account activity metrics cannot be null.");
        } else {
            validateActivityMetrics(activity, errors, warnings);
        }

        boolean isValid = errors.isEmpty();
        return new ValidationResult(isValid, errors, warnings);
    }

    private static void validateActivityMetrics(AccountActivity activity,
                                                List<String> errors,
                                                List<String> warnings) {
        if (activity.getAverageLikes() < 0) {
            errors.add("Average likes cannot be negative.");
        }
        if (activity.getAverageComments() < 0) {
            errors.add("Average comments cannot be negative.");
        }
        if (activity.getRepeatedComments() < 0) {
            errors.add("Repeated comments cannot be negative.");
        }
        if (activity.getTotalCommentsObserved() < 0) {
            errors.add("Total comments observed cannot be negative.");
        }
        if (activity.getCommentsInTenMinutes() < 0) {
            errors.add("Comments in 10 minutes cannot be negative.");
        }

        // Logical constraint: repeated comments cannot exceed total observed comments
        if (activity.getRepeatedComments() > activity.getTotalCommentsObserved()) {
            errors.add(String.format(
                    "Repeated comments (%d) cannot exceed total comments observed (%d).",
                    activity.getRepeatedComments(), activity.getTotalCommentsObserved()));
        }
    }

    /**
     * Checks if a string can be safely parsed as a non-negative integer.
     */
    public static boolean isNonNegativeInteger(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            int val = Integer.parseInt(str.trim());
            return val >= 0;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Checks if a string can be safely parsed as a non-negative double.
     */
    public static boolean isNonNegativeDouble(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            double val = Double.parseDouble(str.trim());
            return val >= 0.0 && !Double.isInfinite(val) && !Double.isNaN(val);
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
