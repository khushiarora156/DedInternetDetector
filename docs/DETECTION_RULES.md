# Detection Rules & Scoring Heuristics

This document details the mathematical models, thresholds, weights, and rationales behind each heuristic rule implemented in the **Dead Internet & Fake Engagement Detector**.

---

## 1. Risk Scoring & Classification System

The engine assigns risk points to triggered rules and computes an explainable aggregate score:

$$\text{Raw Score} = \sum_{i=1}^{N} \text{Points}(\text{Rule}_i)$$

$$\text{Risk Score} = \min(100, \max(0, \text{Raw Score}))$$

### Risk Level Tiers

| Score Range | Risk Level | Meaning & Action Recommendation |
| :---: | :---: | :--- |
| **0 – 29** | `LOW` | **Minimal Suspicion**: Normal human engagement patterns. No moderation required. |
| **30 – 59** | `MEDIUM` | **Moderate Suspicion**: Exhibits single or borderline anomalies. Recommended for queuing in human review audit workflows. |
| **60 – 100** | `HIGH` | **Strong Bot Indicators**: Exhibits multiple concurrent automated indicators. High statistical correlation with botnet / spam campaigns. |

---

## 2. Implemented Detection Rules

### Rule 1: New Account Heuristic (`NewAccountRule`)
- **Metric Checked**: $\text{accountAgeDays} < T_{\text{age}}$
- **Default Threshold ($T_{\text{age}}$)**: 30 days
- **Weight**: 15 Risk Points
- **Rationale**: Disposable bot farms spin up fresh accounts continuously to conduct rapid astroturfing campaigns before platform trust and safety algorithms ban them.
- **Viva Defense**: "Why 30 days?" 30 days is standard industry convention for a probationary account status. It assigns a mild 15 points, meaning a new human user with 0 other flags remains safely in the `LOW` risk category (15/100).

---

### Rule 2: Repetitive Comment Heuristic (`RepetitiveCommentRule`)
- **Metric Checked**:
  $$\text{Ratio} = \frac{\text{repeatedComments}}{\text{totalCommentsObserved}} \ge T_{\text{rep}} \quad (\text{subject to } \text{totalCommentsObserved} \ge S_{\min})$$
- **Default Threshold ($T_{\text{rep}}$)**: 50% ($0.50$)
- **Minimum Sample Size ($S_{\min}$)**: 5 comments
- **Weight**: 20 Risk Points
- **Rationale**: Human commenters provide varied responses across different contexts. Automated scripts, scrapers, and spam bots frequently post copy-pasted templates or promotional links.
- **Guardrail**: If an account has only posted 1 or 2 comments, even if both are identical (100%), the rule will **not** trigger because the sample size is below $S_{\min} = 5$. This prevents false positives on casual human users.

---

### Rule 3: Unusual Engagement Rate Heuristic (`UnusualEngagementRule`)
- **Metric Checked**:
  $$\text{Engagement Rate} = \frac{\text{averageLikes} + \text{averageComments}}{\text{followers}} \times 100 \ge T_{\text{eng}}$$
- **Default Threshold ($T_{\text{eng}}$)**: 25.0%
- **Weight**: 15 Risk Points
- **Rationale**: Standard organic engagement rates across platforms like Instagram, X (Twitter), and YouTube typically average between 1.0% and 4.0%. Rates exceeding 25% are statistically anomalous and usually signify artificial engagement booster pods or click farms.
- **Edge Case (Zero Followers)**:
  - If $\text{followers} == 0 \land (\text{averageLikes} > 0 \lor \text{averageComments} > 0)$, the rule triggers because an account cannot authentically receive interactions without an audience.
  - If $\text{followers} == 0 \land \text{interactions} == 0$, the rule safely does **not** trigger.

---

### Rule 4: Rapid Comment Burst Heuristic (`RapidActivityRule`)
- **Metric Checked**: $\text{commentsInTenMinutes} > T_{\text{burst}}$
- **Default Threshold ($T_{\text{burst}}$)**: 20 comments
- **Weight**: 15 Risk Points
- **Rationale**: Posting more than 20 thoughtful comments across discussion threads in 10 minutes requires an average velocity of one comment every 30 seconds continuously. This strongly indicates scripted, headless browser, or API-automated activity.

---

### Rule 5: Suspicious Follow Ratio Heuristic (`SuspiciousFollowRatioRule`)
- **Metric Checked**:
  $$\text{Follow Ratio} = \frac{\text{following}}{\max(1, \text{followers})} \ge T_{\text{ratio}} \quad (\text{subject to } \text{following} \ge F_{\min})$$
- **Default Threshold ($T_{\text{ratio}}$)**: 10.0x
- **Minimum Following Threshold ($F_{\min}$)**: 150 accounts
- **Weight**: 15 Risk Points
- **Rationale**: Spammer bots aggressively mass-follow accounts hoping for reciprocity ("follow-back"), resulting in profiles following thousands of users while retaining very few followers.
- **Newcomer Protection Guardrail**: A real human who just joined and follows 20 friends with 1 follower has a ratio of 20x. The rule protects such personal accounts by requiring $\text{following} \ge 150$ before this rule can trigger.

---

## 3. Important Ethical Disclaimer & Non-Definitive Stance

> [!CAUTION]
> **Core Ethical Principle**:
> The application calculates a **heuristic suspicion score**, NOT a definitive verdict. 
> 
> No automated system can definitively declare an account to be a bot without server-side telemetric proof (e.g., CAPTCHA failure, TCP handshake fingerprints, biometric keystroke cadence). High risk scores serve solely as a triage tool for human moderators.
