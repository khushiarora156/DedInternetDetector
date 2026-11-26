package com.detector.analysis;

import com.detector.model.RiskLevel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RiskScoreCalculator & RiskLevel Classification Unit Tests")
public class RiskScoreCalculatorTest {

    @Test
    @DisplayName("Should return 0 for empty or null triggered rules")
    void testEmptyRules() {
        assertEquals(0, RiskScoreCalculator.calculateScore(null));
        assertEquals(0, RiskScoreCalculator.calculateScore(Collections.emptyList()));
    }

    @Test
    @DisplayName("Should sum points from multiple triggered rules")
    void testSumPoints() {
        List<DetectionRule> rules = Arrays.asList(
                new NewAccountRule(30, 15),
                new RepetitiveCommentRule(0.5, 5, 20),
                new RapidActivityRule(20, 15)
        );

        // 15 + 20 + 15 = 50
        assertEquals(50, RiskScoreCalculator.calculateScore(rules));
    }

    @Test
    @DisplayName("Should cap score at exactly 100 even if raw points exceed 100")
    void testCapScoreAt100() {
        List<DetectionRule> rules = Arrays.asList(
                new NewAccountRule(30, 40),
                new RepetitiveCommentRule(0.5, 5, 40),
                new RapidActivityRule(20, 40)
        );

        // 40 + 40 + 40 = 120 -> capped to 100
        assertEquals(100, RiskScoreCalculator.calculateScore(rules));
        assertEquals(100, RiskScoreCalculator.capScore(150));
        assertEquals(0, RiskScoreCalculator.capScore(-10));
    }

    @Test
    @DisplayName("Should correctly classify RiskLevels across boundaries")
    void testRiskLevelBoundaries() {
        // LOW: 0 - 29
        assertEquals(RiskLevel.LOW, RiskScoreCalculator.determineRiskLevel(0));
        assertEquals(RiskLevel.LOW, RiskScoreCalculator.determineRiskLevel(15));
        assertEquals(RiskLevel.LOW, RiskScoreCalculator.determineRiskLevel(29));

        // MEDIUM: 30 - 59
        assertEquals(RiskLevel.MEDIUM, RiskScoreCalculator.determineRiskLevel(30));
        assertEquals(RiskLevel.MEDIUM, RiskScoreCalculator.determineRiskLevel(45));
        assertEquals(RiskLevel.MEDIUM, RiskScoreCalculator.determineRiskLevel(59));

        // HIGH: 60 - 100
        assertEquals(RiskLevel.HIGH, RiskScoreCalculator.determineRiskLevel(60));
        assertEquals(RiskLevel.HIGH, RiskScoreCalculator.determineRiskLevel(85));
        assertEquals(RiskLevel.HIGH, RiskScoreCalculator.determineRiskLevel(100));
    }
}
