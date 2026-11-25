package com.detector.validation;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DataValidator Unit Tests")
public class DataValidatorTest {

    @Test
    @DisplayName("Should pass valid account")
    void testValidAccount() {
        AccountActivity act = new AccountActivity(20.0, 5.0, 2, 10, 3);
        SocialMediaAccount acc = new SocialMediaAccount("ACC_01", "valid_user", 500, 300, 100, 200, act);

        ValidationResult result = DataValidator.validateAccount(acc);
        assertTrue(result.isValid());
        assertTrue(result.getErrors().isEmpty());
    }

    @Test
    @DisplayName("Should fail when account is null")
    void testNullAccount() {
        ValidationResult result = DataValidator.validateAccount(null);
        assertFalse(result.isValid());
        assertTrue(result.getErrors().contains("Account object is null."));
    }

    @Test
    @DisplayName("Should fail on invalid account ID or username")
    void testInvalidIdAndUsername() {
        AccountActivity act = new AccountActivity(10, 2, 1, 5, 1);
        SocialMediaAccount acc = new SocialMediaAccount("A", "inv@lid user!", 100, 50, 10, 20, act);

        ValidationResult result = DataValidator.validateAccount(acc);
        assertFalse(result.isValid());
        assertTrue(result.getFormattedErrors().contains("Account ID must be 2-30"));
        assertTrue(result.getFormattedErrors().contains("Username must be 2-30"));
    }

    @Test
    @DisplayName("Should fail when repeated comments exceed total comments observed")
    void testRepeatedCommentsExceedTotal() {
        AccountActivity act = new AccountActivity(10, 2, 15, 10, 1);
        SocialMediaAccount acc = new SocialMediaAccount("ACC_02", "bot_tester", 100, 50, 10, 20, act);

        ValidationResult result = DataValidator.validateAccount(acc);
        assertFalse(result.isValid());
        assertTrue(result.getFormattedErrors().contains("cannot exceed total comments"));
    }

    @Test
    @DisplayName("Should correctly validate helper parsing methods")
    void testHelperValidationMethods() {
        assertTrue(DataValidator.isNonNegativeInteger("0"));
        assertTrue(DataValidator.isNonNegativeInteger("12345"));
        assertFalse(DataValidator.isNonNegativeInteger("-1"));
        assertFalse(DataValidator.isNonNegativeInteger("abc"));
        assertFalse(DataValidator.isNonNegativeInteger(""));

        assertTrue(DataValidator.isNonNegativeDouble("0.0"));
        assertTrue(DataValidator.isNonNegativeDouble("12.45"));
        assertFalse(DataValidator.isNonNegativeDouble("-3.14"));
        assertFalse(DataValidator.isNonNegativeDouble("xyz"));
    }
}
