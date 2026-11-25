package com.detector.input;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;

/**
 * Converts parsed string tokens into strongly-typed SocialMediaAccount and AccountActivity domain models.
 * Isolates data type parsing and defensive conversion from file streaming.
 */
public class AccountDataMapper {

    /**
     * Maps an array of 11 string tokens into a SocialMediaAccount with an attached AccountActivity.
     *
     * @param tokens token array from CsvLineParser
     * @return constructed SocialMediaAccount
     * @throws IllegalArgumentException if numbers cannot be parsed
     */
    public SocialMediaAccount mapTokens(String[] tokens) {
        if (tokens == null || tokens.length < CsvLineParser.EXPECTED_COLUMN_COUNT) {
            throw new IllegalArgumentException("Insufficient tokens for account mapping.");
        }

        try {
            String accountId = tokens[0];
            String username = tokens[1];
            int followers = Integer.parseInt(tokens[2]);
            int following = Integer.parseInt(tokens[3]);
            int totalPosts = Integer.parseInt(tokens[4]);
            int accountAgeDays = Integer.parseInt(tokens[5]);

            double averageLikes = Double.parseDouble(tokens[6]);
            double averageComments = Double.parseDouble(tokens[7]);
            int repeatedComments = Integer.parseInt(tokens[8]);
            int totalCommentsObserved = Integer.parseInt(tokens[9]);
            int commentsInTenMinutes = Integer.parseInt(tokens[10]);

            AccountActivity activity = new AccountActivity(
                    averageLikes,
                    averageComments,
                    repeatedComments,
                    totalCommentsObserved,
                    commentsInTenMinutes
            );

            return new SocialMediaAccount(
                    accountId,
                    username,
                    followers,
                    following,
                    totalPosts,
                    accountAgeDays,
                    activity
            );
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Invalid numeric value in row: " + e.getMessage(), e);
        }
    }
}
