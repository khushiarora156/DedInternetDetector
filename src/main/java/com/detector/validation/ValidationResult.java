package com.detector.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Encapsulates the outcome of validating account data, storing any validation errors and warnings.
 */
public class ValidationResult {
    private final boolean valid;
    private final List<String> errors;
    private final List<String> warnings;

    public ValidationResult(boolean valid, List<String> errors, List<String> warnings) {
        this.valid = valid;
        this.errors = (errors != null) ? new ArrayList<>(errors) : new ArrayList<>();
        this.warnings = (warnings != null) ? new ArrayList<>(warnings) : new ArrayList<>();
    }

    public static ValidationResult success() {
        return new ValidationResult(true, Collections.emptyList(), Collections.emptyList());
    }

    public static ValidationResult failure(List<String> errors) {
        return new ValidationResult(false, errors, Collections.emptyList());
    }

    public static ValidationResult failure(String errorMessage) {
        List<String> list = new ArrayList<>();
        list.add(errorMessage);
        return new ValidationResult(false, list, Collections.emptyList());
    }

    public boolean isValid() {
        return valid;
    }

    public List<String> getErrors() {
        return Collections.unmodifiableList(errors);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public String getFormattedErrors() {
        return String.join("; ", errors);
    }

    @Override
    public String toString() {
        if (valid) {
            return "ValidationResult[VALID]";
        } else {
            return "ValidationResult[INVALID: " + getFormattedErrors() + "]";
        }
    }
}
