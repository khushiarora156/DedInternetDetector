package com.detector.input;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;
import com.detector.validation.DataValidator;
import com.detector.validation.ValidationResult;

import java.util.Scanner;

/**
 * Handles interactive, terminal-based manual input of account records.
 * Provides user-friendly input validation, type-checking, and automatic retry prompts.
 */
public class ManualInput {
    private final Scanner scanner;

    public ManualInput(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Prompts the user step-by-step to create a validated SocialMediaAccount.
     *
     * @return constructed SocialMediaAccount or null if cancelled
     */
    public SocialMediaAccount promptForAccount() {
        System.out.println("\n----------------------------------------");
        System.out.println("       MANUAL ACCOUNT REGISTRATION      ");
        System.out.println("----------------------------------------");
        System.out.println("Enter account details below (type 'cancel' to abort):");

        String accountId = promptForNonEmptyString("Account ID (e.g., ACC_101): ");
        if (accountId == null) return null;

        String username = promptForNonEmptyString("Username / Handle (without @, e.g., tech_user): ");
        if (username == null) return null;

        Integer followers = promptForNonNegativeInt("Followers count: ");
        if (followers == null) return null;

        Integer following = promptForNonNegativeInt("Following count: ");
        if (following == null) return null;

        Integer totalPosts = promptForNonNegativeInt("Total Posts: ");
        if (totalPosts == null) return null;

        Integer accountAgeDays = promptForNonNegativeInt("Account Age (in days): ");
        if (accountAgeDays == null) return null;

        System.out.println("\n--- Observed Activity Metrics ---");
        Double avgLikes = promptForNonNegativeDouble("Average Likes per post: ");
        if (avgLikes == null) return null;

        Double avgComments = promptForNonNegativeDouble("Average Comments per post: ");
        if (avgComments == null) return null;

        Integer totalComments = promptForNonNegativeInt("Total Comments observed: ");
        if (totalComments == null) return null;

        Integer repeatedComments = promptForRepeatedComments("Repeated/Templated Comments: ", totalComments);
        if (repeatedComments == null) return null;

        Integer burstComments = promptForNonNegativeInt("Comments posted in 10-minute window: ");
        if (burstComments == null) return null;

        AccountActivity activity = new AccountActivity(
                avgLikes, avgComments, repeatedComments, totalComments, burstComments
        );

        SocialMediaAccount account = new SocialMediaAccount(
                accountId, username, followers, following, totalPosts, accountAgeDays, activity
        );

        ValidationResult validation = DataValidator.validateAccount(account);
        if (!validation.isValid()) {
            System.out.println("\n[!] Validation Failed: " + validation.getFormattedErrors());
            return null;
        }

        return account;
    }

    private String promptForNonEmptyString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("[*] Operation cancelled by user.");
                return null;
            }
            if (!input.isEmpty()) {
                return input;
            }
            System.out.println("[!] Input cannot be blank. Please try again.");
        }
    }

    private Integer promptForNonNegativeInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("[*] Operation cancelled by user.");
                return null;
            }
            try {
                int val = Integer.parseInt(input);
                if (val >= 0) {
                    return val;
                }
                System.out.println("[!] Value must be 0 or positive. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number format. Please enter an integer.");
            }
        }
    }

    private Double promptForNonNegativeDouble(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.equalsIgnoreCase("cancel")) {
                System.out.println("[*] Operation cancelled by user.");
                return null;
            }
            try {
                double val = Double.parseDouble(input);
                if (val >= 0.0 && !Double.isInfinite(val) && !Double.isNaN(val)) {
                    return val;
                }
                System.out.println("[!] Value must be 0.0 or positive. Please try again.");
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid decimal format. Please enter a number (e.g., 14.5).");
            }
        }
    }

    private Integer promptForRepeatedComments(String prompt, int totalObserved) {
        while (true) {
            Integer repeated = promptForNonNegativeInt(prompt);
            if (repeated == null) return null;
            if (repeated <= totalObserved) {
                return repeated;
            }
            System.out.printf("[!] Repeated comments (%d) cannot exceed total comments observed (%d).%n",
                    repeated, totalObserved);
        }
    }
}
