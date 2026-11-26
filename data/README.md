# Sample Dataset: Simulated Social Media Accounts

This directory contains simulated social media profile data designed for testing and demonstrating the **Dead Internet & Fake Engagement Detector**.

> [!NOTE]
> **SIMULATED DATA DISCLOSURE**:
> All accounts, usernames, and metrics in `accounts.csv` are **100% synthetically generated** for educational and demonstration purposes. No real personal data or social media API queries were utilized.

---

## CSV File Format

The dataset `accounts.csv` adheres to the following 11-column format:

| Column Header | Data Type | Constraint | Description |
| :--- | :--- | :--- | :--- |
| `accountId` | String | Unique, non-blank | Primary identifier for the account (e.g. `ACC_001`). |
| `username` | String | Alphanumeric handle | Public handle of the account (without leading `@`). |
| `followers` | Integer | $\ge 0$ | Number of followers subscribed to this account. |
| `following` | Integer | $\ge 0$ | Number of accounts this account follows. |
| `totalPosts` | Integer | $\ge 0$ | Total lifetime posts published. |
| `accountAgeDays` | Integer | $\ge 0$ | Number of days elapsed since profile creation. |
| `averageLikes` | Double | $\ge 0.0$ | Observed average number of likes received per post. |
| `averageComments` | Double | $\ge 0.0$ | Observed average number of comments received per post. |
| `repeatedComments`| Integer | $\ge 0 \land \le \text{total}$ | Count of identical, templated, or copy-pasted comments. |
| `totalCommentsObserved`| Integer | $\ge 0$ | Sample window of total comments analyzed. |
| `commentsInTenMinutes`| Integer | $\ge 0$ | Highest burst frequency of comments posted within 10 minutes. |

---

## Included Behavioral Profiles

1. **Authentic / Organic Users** (`ACC_001`, `ACC_002`, `ACC_007`, `ACC_010`, `ACC_016`):
   - Consistent engagement rates (1% – 4%).
   - Low repeated comments (< 10%).
   - Balanced following-to-follower ratios.
   - Low burst activity ($\le 4$ comments / 10 min).

2. **Hyper-Velocity Comment Spammers** (`ACC_004`, `ACC_014`):
   - High burst frequency ($> 20$ comments in 10 minutes), exceeding human typing and navigation velocity.

3. **Copypasta & Echo Bots** (`ACC_005`, `ACC_013`):
   - High repetitive comment ratios ($> 50\%$), posting identical text across multiple posts.

4. **Synthetic Engagement & Click Farms** (`ACC_006`, `ACC_012`):
   - Inflated engagement rates ($> 25\%$), where average interactions exceed plausible organic reach.

5. **Mass-Follow Churners** (`ACC_011`):
   - Following thousands of accounts with negligible followers and minimal interaction.

6. **Multi-Vector Bot Swarms** (`ACC_003`, `ACC_009`, `ACC_015`):
   - Fresh account creation ($< 30$ days).
   - High follow ratios ($> 10\text{x}$).
   - High repetitive comment ratios ($> 80\%$).
   - High burst comment frequency ($> 25$ in 10 minutes).
   - Triggers 4 or 5 rules simultaneously, reaching **HIGH RISK** status.
