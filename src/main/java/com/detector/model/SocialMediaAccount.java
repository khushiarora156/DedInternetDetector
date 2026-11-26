package com.detector.model;

import java.util.Objects;

/**
 * Represents a social media account profile and its attached activity data.
 * Adheres strictly to Java OOP encapsulation, standard accessors, and defensive validation.
 */
public class SocialMediaAccount {
    private String accountId;
    private String username;
    private int followers;
    private int following;
    private int totalPosts;
    private int accountAgeDays;
    private AccountActivity activity;

    /**
     * Default constructor initializing with default activity.
     */
    public SocialMediaAccount() {
        this.activity = new AccountActivity();
    }

    /**
     * Parameterized constructor for a social media account.
     *
     * @param accountId      Unique account identifier
     * @param username       Public handle / username
     * @param followers      Follower count (>= 0)
     * @param following      Following count (>= 0)
     * @param totalPosts     Total lifetime posts (>= 0)
     * @param accountAgeDays Age of account in days (>= 0)
     * @param activity       Observed activity profile (null safe: defaults to empty)
     */
    public SocialMediaAccount(String accountId, String username, int followers,
                              int following, int totalPosts, int accountAgeDays,
                              AccountActivity activity) {
        setAccountId(accountId);
        setUsername(username);
        setFollowers(followers);
        setFollowing(following);
        setTotalPosts(totalPosts);
        setAccountAgeDays(accountAgeDays);
        setActivity(activity);
    }

    public String getAccountId() {
        return accountId;
    }

    public void setAccountId(String accountId) {
        if (accountId == null || accountId.trim().isEmpty()) {
            throw new IllegalArgumentException("Account ID cannot be null or empty.");
        }
        this.accountId = accountId.trim();
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username cannot be null or empty.");
        }
        this.username = username.trim();
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        if (followers < 0) {
            throw new IllegalArgumentException("Followers cannot be negative: " + followers);
        }
        this.followers = followers;
    }

    public int getFollowing() {
        return following;
    }

    public void setFollowing(int following) {
        if (following < 0) {
            throw new IllegalArgumentException("Following cannot be negative: " + following);
        }
        this.following = following;
    }

    public int getTotalPosts() {
        return totalPosts;
    }

    public void setTotalPosts(int totalPosts) {
        if (totalPosts < 0) {
            throw new IllegalArgumentException("Total posts cannot be negative: " + totalPosts);
        }
        this.totalPosts = totalPosts;
    }

    public int getAccountAgeDays() {
        return accountAgeDays;
    }

    public void setAccountAgeDays(int accountAgeDays) {
        if (accountAgeDays < 0) {
            throw new IllegalArgumentException("Account age in days cannot be negative: " + accountAgeDays);
        }
        this.accountAgeDays = accountAgeDays;
    }

    public AccountActivity getActivity() {
        return activity;
    }

    public void setActivity(AccountActivity activity) {
        this.activity = (activity != null) ? activity : new AccountActivity();
    }

    /**
     * Calculates the follow ratio (following divided by followers).
     * If followers is 0, returns the following count directly as a representation of infinity.
     *
     * @return follow ratio
     */
    public double getFollowRatio() {
        if (followers == 0) {
            return following > 0 ? (double) following : 0.0;
        }
        return (double) following / followers;
    }

    /**
     * Calculates the engagement rate percentage: ((avgLikes + avgComments) / followers) * 100.
     * Safely returns 0.0 if the account has 0 followers.
     *
     * @return engagement rate percentage
     */
    public double getEngagementRate() {
        if (followers <= 0) {
            return 0.0;
        }
        return ((activity.getAverageLikes() + activity.getAverageComments()) / followers) * 100.0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SocialMediaAccount that = (SocialMediaAccount) o;
        return Objects.equals(accountId, that.accountId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(accountId);
    }

    @Override
    public String toString() {
        return String.format(
            "Account[ID='%s', User='@%s', Followers=%d, Following=%d, Posts=%d, Age=%dd, %s]",
            accountId, username, followers, following, totalPosts, accountAgeDays, activity
        );
    }
}
