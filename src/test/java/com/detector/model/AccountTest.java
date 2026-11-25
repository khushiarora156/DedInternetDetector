package com.detector.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SocialMediaAccount & AccountActivity Unit Tests")
public class AccountTest {

    private AccountActivity defaultActivity;
    private SocialMediaAccount testAccount;

    @BeforeEach
    void setUp() {
        defaultActivity = new AccountActivity(50.0, 10.0, 5, 20, 8);
        testAccount = new SocialMediaAccount("ACC_100", "testuser", 1000, 200, 150, 90, defaultActivity);
    }

    @Test
    @DisplayName("Should successfully create account with valid inputs")
    void testAccountCreationValid() {
        assertEquals("ACC_100", testAccount.getAccountId());
        assertEquals("testuser", testAccount.getUsername());
        assertEquals(1000, testAccount.getFollowers());
        assertEquals(200, testAccount.getFollowing());
        assertEquals(150, testAccount.getTotalPosts());
        assertEquals(90, testAccount.getAccountAgeDays());
        assertNotNull(testAccount.getActivity());
    }

    @Test
    @DisplayName("Should calculate correct follow ratio")
    void testFollowRatioCalculation() {
        // following = 200, followers = 1000 -> ratio = 0.2
        assertEquals(0.2, testAccount.getFollowRatio(), 0.001);

        // Zero followers case
        SocialMediaAccount zeroFollowers = new SocialMediaAccount("ACC_101", "zero", 0, 50, 10, 20, defaultActivity);
        assertEquals(50.0, zeroFollowers.getFollowRatio(), 0.001);
    }

    @Test
    @DisplayName("Should calculate correct engagement rate and handle zero followers safely")
    void testEngagementRateCalculation() {
        // (50 likes + 10 comments) / 1000 followers * 100 = 6.0%
        assertEquals(6.0, testAccount.getEngagementRate(), 0.001);

        // Zero followers should safely yield 0.0 without divide-by-zero exception
        SocialMediaAccount zeroFollowers = new SocialMediaAccount("ACC_102", "zero_fol", 0, 100, 5, 10, defaultActivity);
        assertEquals(0.0, zeroFollowers.getEngagementRate(), 0.001);
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException on negative metrics")
    void testNegativeMetricsValidation() {
        assertThrows(IllegalArgumentException.class, () -> testAccount.setFollowers(-5));
        assertThrows(IllegalArgumentException.class, () -> testAccount.setFollowing(-1));
        assertThrows(IllegalArgumentException.class, () -> testAccount.setTotalPosts(-10));
        assertThrows(IllegalArgumentException.class, () -> testAccount.setAccountAgeDays(-30));
        assertThrows(IllegalArgumentException.class, () -> defaultActivity.setAverageLikes(-2.0));
        assertThrows(IllegalArgumentException.class, () -> defaultActivity.setAverageComments(-1.0));
        assertThrows(IllegalArgumentException.class, () -> defaultActivity.setRepeatedComments(-1));
        assertThrows(IllegalArgumentException.class, () -> defaultActivity.setTotalCommentsObserved(-5));
        assertThrows(IllegalArgumentException.class, () -> defaultActivity.setCommentsInTenMinutes(-2));
    }

    @Test
    @DisplayName("Should calculate repetitive comment ratio safely")
    void testRepetitiveCommentRatio() {
        // 5 repeated out of 20 observed = 0.25
        assertEquals(0.25, defaultActivity.getRepetitiveCommentRatio(), 0.001);

        // Zero total comments should safely return 0.0
        AccountActivity zeroComments = new AccountActivity(10, 5, 0, 0, 0);
        assertEquals(0.0, zeroComments.getRepetitiveCommentRatio(), 0.001);
    }
}
