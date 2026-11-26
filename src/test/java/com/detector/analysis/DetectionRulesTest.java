package com.detector.analysis;

import com.detector.model.AccountActivity;
import com.detector.model.SocialMediaAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Detection Rules Unit & Edge Case Tests")
public class DetectionRulesTest {

    @Test
    @DisplayName("NewAccountRule: flags < 30 days, passes >= 30 days")
    void testNewAccountRule() {
        NewAccountRule rule = new NewAccountRule(30, 15);

        // Account 10 days old -> triggered
        SocialMediaAccount young = new SocialMediaAccount("A1", "u1", 100, 50, 10, 10, new AccountActivity());
        assertTrue(rule.isTriggered(young));
        assertEquals(15, rule.getRiskPoints());
        assertTrue(rule.getExplanation(young).contains("10 day(s)"));

        // Account 30 days old (boundary) -> not triggered (< 30)
        SocialMediaAccount boundary = new SocialMediaAccount("A2", "u2", 100, 50, 10, 30, new AccountActivity());
        assertFalse(rule.isTriggered(boundary));

        // Account 100 days old -> not triggered
        SocialMediaAccount old = new SocialMediaAccount("A3", "u3", 100, 50, 10, 100, new AccountActivity());
        assertFalse(rule.isTriggered(old));
    }

    @Test
    @DisplayName("RepetitiveCommentRule: flags high ratio with min sample size, ignores small samples")
    void testRepetitiveCommentRule() {
        RepetitiveCommentRule rule = new RepetitiveCommentRule(0.50, 5, 20);

        // 18 repeated out of 20 observed (90%) -> triggered
        AccountActivity actSpam = new AccountActivity(10, 2, 18, 20, 2);
        SocialMediaAccount spammer = new SocialMediaAccount("A1", "spam", 100, 50, 10, 100, actSpam);
        assertTrue(rule.isTriggered(spammer));
        assertEquals(20, rule.getRiskPoints());

        // 2 repeated out of 20 observed (10%) -> not triggered
        AccountActivity actNormal = new AccountActivity(10, 2, 2, 20, 2);
        SocialMediaAccount normal = new SocialMediaAccount("A2", "norm", 100, 50, 10, 100, actNormal);
        assertFalse(rule.isTriggered(normal));

        // 2 repeated out of 2 observed (100%), but sample size < 5 -> should NOT trigger (guardrail)
        AccountActivity actSmallSample = new AccountActivity(10, 2, 2, 2, 1);
        SocialMediaAccount smallSample = new SocialMediaAccount("A3", "small", 100, 50, 10, 100, actSmallSample);
        assertFalse(rule.isTriggered(smallSample));
    }

    @Test
    @DisplayName("UnusualEngagementRule: flags high engagement rate, handles 0 followers safely")
    void testUnusualEngagementRule() {
        UnusualEngagementRule rule = new UnusualEngagementRule(25.0, 15);

        // 100 followers, avg 50 likes + 10 comments = 60 interactions -> 60% engagement (> 25%) -> triggered
        AccountActivity actHigh = new AccountActivity(50, 10, 0, 5, 1);
        SocialMediaAccount bot = new SocialMediaAccount("A1", "booster", 100, 50, 10, 100, actHigh);
        assertTrue(rule.isTriggered(bot));

        // 1000 followers, avg 10 likes + 2 comments = 12 interactions -> 1.2% engagement -> not triggered
        AccountActivity actNormal = new AccountActivity(10, 2, 0, 5, 1);
        SocialMediaAccount normal = new SocialMediaAccount("A2", "human", 1000, 50, 10, 100, actNormal);
        assertFalse(rule.isTriggered(normal));

        // Zero followers with interactions -> synthetic activity -> triggered
        SocialMediaAccount zeroFollowersWithLikes = new SocialMediaAccount("A3", "ghost", 0, 10, 5, 10, actHigh);
        assertTrue(rule.isTriggered(zeroFollowersWithLikes));

        // Zero followers with 0 interactions -> genuine new/inactive user -> not triggered
        AccountActivity actZero = new AccountActivity(0, 0, 0, 0, 0);
        SocialMediaAccount zeroFollowersZeroLikes = new SocialMediaAccount("A4", "newbie", 0, 5, 1, 1, actZero);
        assertFalse(rule.isTriggered(zeroFollowersZeroLikes));
    }

    @Test
    @DisplayName("RapidActivityRule: flags burst comments > 20 in 10 minutes")
    void testRapidActivityRule() {
        RapidActivityRule rule = new RapidActivityRule(20, 15);

        // 25 comments in 10 mins -> triggered
        AccountActivity actBurst = new AccountActivity(5, 1, 0, 30, 25);
        SocialMediaAccount burstUser = new SocialMediaAccount("A1", "fast", 200, 100, 50, 100, actBurst);
        assertTrue(rule.isTriggered(burstUser));

        // 20 comments in 10 mins (boundary) -> not triggered (> 20)
        AccountActivity actBoundary = new AccountActivity(5, 1, 0, 30, 20);
        SocialMediaAccount boundaryUser = new SocialMediaAccount("A2", "bound", 200, 100, 50, 100, actBoundary);
        assertFalse(rule.isTriggered(boundaryUser));

        // 3 comments in 10 mins -> not triggered
        AccountActivity actHuman = new AccountActivity(5, 1, 0, 30, 3);
        SocialMediaAccount human = new SocialMediaAccount("A3", "human", 200, 100, 50, 100, actHuman);
        assertFalse(rule.isTriggered(human));
    }

    @Test
    @DisplayName("SuspiciousFollowRatioRule: flags high follow ratios, protects small personal accounts")
    void testSuspiciousFollowRatioRule() {
        SuspiciousFollowRatioRule rule = new SuspiciousFollowRatioRule(10.0, 150, 15);

        // Spammer: follows 2,500 people, only 50 followers (ratio: 50x) -> triggered
        SocialMediaAccount spammer = new SocialMediaAccount("A1", "spammer", 50, 2500, 10, 50, new AccountActivity());
        assertTrue(rule.isTriggered(spammer));

        // Normal user: follows 200, 400 followers (ratio: 0.5x) -> not triggered
        SocialMediaAccount normal = new SocialMediaAccount("A2", "normal", 400, 200, 10, 50, new AccountActivity());
        assertFalse(rule.isTriggered(normal));

        // Newcomer guardrail: follows 50 people, has 2 followers (ratio: 25x, but following < 150) -> NOT triggered
        SocialMediaAccount smallPersonal = new SocialMediaAccount("A3", "personal", 2, 50, 1, 5, new AccountActivity());
        assertFalse(rule.isTriggered(smallPersonal));
    }
}
