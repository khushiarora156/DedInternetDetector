package com.detector.service;

import com.detector.model.AccountActivity;
import com.detector.model.AnalysisResult;
import com.detector.model.SocialMediaAccount;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multithreading Concurrency & Consistency Tests")
public class MultithreadingConsistencyTest {

    @Test
    @DisplayName("Parallel and sequential analysis must yield identical results for all accounts")
    void testSequentialVsParallelConsistency() {
        AnalysisService service = new AnalysisService();

        // Populate service with varied accounts
        service.registerAccount(new SocialMediaAccount("ACC_01", "normal1", 1000, 200, 50, 400,
                new AccountActivity(20, 2, 1, 20, 2)));
        service.registerAccount(new SocialMediaAccount("ACC_02", "bot_new", 50, 2500, 10, 12,
                new AccountActivity(2, 0.5, 18, 20, 28)));
        service.registerAccount(new SocialMediaAccount("ACC_03", "booster", 100, 300, 25, 200,
                new AccountActivity(80, 40, 2, 20, 3)));
        service.registerAccount(new SocialMediaAccount("ACC_04", "spammer", 300, 200, 40, 150,
                new AccountActivity(5, 1, 0, 30, 35)));
        service.registerAccount(new SocialMediaAccount("ACC_05", "normal2", 5000, 300, 120, 800,
                new AccountActivity(150, 20, 2, 40, 4)));

        // Run sequential
        List<AnalysisResult> seqResults = service.analyzeAllSequential();

        // Run parallel with 4 threads
        List<AnalysisResult> parResults = service.analyzeAllParallel(4);

        assertEquals(seqResults.size(), parResults.size(), "Both runs should analyze same number of accounts");

        // Map by account ID for direct item-by-item verification
        Map<String, AnalysisResult> seqMap = seqResults.stream()
                .collect(Collectors.toMap(AnalysisResult::getAccountId, r -> r));

        for (AnalysisResult par : parResults) {
            AnalysisResult seq = seqMap.get(par.getAccountId());
            assertNotNull(seq, "Account " + par.getAccountId() + " must exist in sequential results");

            assertEquals(seq.getRiskScore(), par.getRiskScore(),
                    "Risk score must match for " + par.getAccountId());
            assertEquals(seq.getRiskLevel(), par.getRiskLevel(),
                    "Risk level must match for " + par.getAccountId());
            assertEquals(seq.getTriggeredRules(), par.getTriggeredRules(),
                    "Triggered rules must match for " + par.getAccountId());
            assertEquals(seq.getExplanations(), par.getExplanations(),
                    "Explanations must match for " + par.getAccountId());
        }
    }
}
