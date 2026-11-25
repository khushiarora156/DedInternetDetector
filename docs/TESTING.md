# Testing Strategy & Test Matrix

This document provides a comprehensive overview of the testing methodology, edge case handling, and automated test execution for the **Dead Internet & Fake Engagement Detector**.

---

## 1. Test Architecture Overview

The project employs a dual-testing strategy:
1. **JUnit 5 Suite (`src/test/java/com/detector/...`)**: Standard Maven test classes organized across packages for CI/CD and developer environments with Maven installed.
2. **Standalone Test Harness (`src/test/java/com/detector/TestHarness.java`)**: A self-contained test runner that compiles directly with standard `javac` and executes with `java`, enabling instant 100% test verification on any environment without Maven installed.

---

## 2. Test Matrix & Coverage

| Test Case | Class Tested | Category | Condition / Input | Expected Result | Verified |
| :--- | :--- | :--- | :--- | :--- | :---: |
| Account Creation | `SocialMediaAccount` | Positive | Valid ID, handle, counts, activity | Object instantiated, accessors return correct values | [x] |
| Follow Ratio | `SocialMediaAccount` | Math / Edge | 250 following / 1000 followers | Ratio = 0.25 | [x] |
| Zero Followers Ratio | `SocialMediaAccount` | Boundary | 80 following / 0 followers | Returns following count (80.0) without crash | [x] |
| Engagement Rate | `SocialMediaAccount` | Math | 50 likes + 10 cmts / 1000 followers | Rate = 6.0% | [x] |
| Zero Followers Engagement | `SocialMediaAccount` | Edge | 0 followers with 0 interactions | Returns 0.0% without division-by-zero error | [x] |
| Negative Input Rejection | `SocialMediaAccount` / `AccountActivity` | Negative | -10 followers, -5.0 likes | Throws `IllegalArgumentException` | [x] |
| DataValidator Pass | `DataValidator` | Positive | Complete valid account | `isValid() == true`, no error messages | [x] |
| DataValidator Null | `DataValidator` | Negative | `null` account reference | `isValid() == false`, reports null error | [x] |
| Repeated > Total Cmts | `DataValidator` | Constraint | repeated = 25, total = 20 | `isValid() == false`, reports constraint error | [x] |
| Duplicate Account ID | `AccountManager` | Integrity | Two accounts with `ACC_001` | Second account rejected (`false`), count unchanged | [x] |
| O(1) Account Retrieval | `AccountManager` | Lookup | Find `ACC_002` via HashMap | Returns account in $O(1)$ time complexity | [x] |
| Account Deletion | `AccountManager` | Mutation | Remove `ACC_001` | Removed from List, Map, and Set; count decremented | [x] |
| Young Account Flag | `NewAccountRule` | Threshold | Age = 10 days ($< 30$) | Rule triggered (+15 points) | [x] |
| Boundary Account Age | `NewAccountRule` | Boundary | Age = 30 days | Rule NOT triggered | [x] |
| Repetitive Comments | `RepetitiveCommentRule` | Threshold | 18 repeated / 20 total (90%) | Rule triggered (+20 points) | [x] |
| Small Sample Guardrail | `RepetitiveCommentRule` | Boundary | 2 repeated / 2 total (sample $< 5$) | Rule NOT triggered (protects sparse users) | [x] |
| Synthetic Engagement | `UnusualEngagementRule` | Anomaly | 0 followers, 50 likes | Rule triggered (+15 points) | [x] |
| High Engagement Rate | `UnusualEngagementRule` | Threshold | Rate = 60.0% ($> 25\%$) | Rule triggered (+15 points) | [x] |
| Rapid Comment Burst | `RapidActivityRule` | Threshold | 25 comments in 10 mins ($> 20$) | Rule triggered (+15 points) | [x] |
| Boundary Comment Burst | `RapidActivityRule` | Boundary | 20 comments in 10 mins | Rule NOT triggered | [x] |
| Spammer Follow Ratio | `SuspiciousFollowRatioRule`| Threshold | 2500 following / 50 followers | Rule triggered (+15 points) | [x] |
| Newcomer Guardrail | `SuspiciousFollowRatioRule`| Protection | 50 following / 2 followers ($< 150$) | Rule NOT triggered (protects personal accounts) | [x] |
| Score Capping at 100 | `RiskScoreCalculator` | Cap | Raw points = 150 | Clamped to 100 | [x] |
| Risk Level LOW | `RiskScoreCalculator` | Range | Score = 0 to 29 | Classified as `RiskLevel.LOW` | [x] |
| Risk Level MEDIUM | `RiskScoreCalculator` | Range | Score = 30 to 59 | Classified as `RiskLevel.MEDIUM` | [x] |
| Risk Level HIGH | `RiskScoreCalculator` | Range | Score = 60 to 100 | Classified as `RiskLevel.HIGH` | [x] |
| Parallel Consistency | `AnalysisService` | Concurrency | Sequential vs. Parallel (4 threads) | Identical scores, levels, and rules across all accounts | [x] |
| CSV Header Skipping | `CSVReader` | I/O | CSV with standard header row | Header ignored, data rows parsed | [x] |
| CSV Error Resilience | `CSVReader` | Fault Tolerance| Row with missing columns & bad types | Skips malformed rows, parses valid rows, reports errors | [x] |
| CSV Report Export | `ReportGenerator` | Export | Generated CSV audit file | File created, valid header, correct quoting | [x] |
| TXT Summary Export | `ReportGenerator` | Export | Generated TXT audit summary | Summary statistics, breakdown, ethical disclaimer | [x] |

---

## 3. How to Run the Tests

### Option A: Using the Standalone Runner (Zero External Dependencies)
```powershell
# Compile classes
javac -d target/classes (Get-ChildItem -Path src/main/java -Recurse -Filter *.java | ForEach-Object { $_.FullName })
javac -cp target/classes -d target/test-classes src/test/java/com/detector/TestHarness.java

# Run test harness
java -cp "target/classes;target/test-classes" com.detector.TestHarness
```

Or simply run the provided test script:
```powershell
.\test.ps1
```

### Option B: Using Apache Maven (When Installed)
```powershell
mvn clean test
```

---

## 4. Verification Results Summary

```
===============================================================
   DEAD INTERNET DETECTOR - STANDALONE VERIFICATION SUITE
===============================================================
[PASS] Account Creation & Accessors
[PASS] Follow Ratio Calculation
[PASS] Engagement Calculation & Zero Followers Handling
[PASS] Account Defensive Negative Validation
[PASS] DataValidator Validation Logic
[PASS] AccountManager Duplicate Prevention & O(1) Lookup
[PASS] NewAccountRule Edge Cases
[PASS] RepetitiveCommentRule & Min Sample Guardrail
[PASS] UnusualEngagementRule & Zero Follower Safety
[PASS] RapidActivityRule & Boundary Threshold
[PASS] SuspiciousFollowRatioRule & Newcomer Protection
[PASS] RiskScoreCalculator Capping & Boundary Classification
[PASS] Sequential vs Multithreaded Parallel Consistency
[PASS] CSVReader Parsing & Malformed Line Resilience
[PASS] ReportGenerator CSV and TXT Export
===============================================================
TEST SUITE SUMMARY: 15 PASSED, 0 FAILED
===============================================================
```
