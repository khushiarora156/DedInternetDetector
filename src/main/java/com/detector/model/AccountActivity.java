package com.detector.model;

import java.util.Objects;

/**
 * Encapsulates activity metrics and comment observation statistics for a social media account.
 * Demonstrates Java encapsulation, defensive validation, and OOP modeling.
 */
public class AccountActivity {
    private double averageLikes;
    private double averageComments;
    private int repeatedComments;
    private int totalCommentsObserved;
    private int commentsInTenMinutes;

    /**
     * Default constructor initializing with zeroed metrics.
     */
    public AccountActivity() {
        this(0.0, 0.0, 0, 0, 0);
    }

    /**
     * Parameterized constructor with validation.
     *
     * @param averageLikes           Average likes per post (non-negative)
     * @param averageComments        Average comments per post (non-negative)
     * @param repeatedComments       Number of repetitive/duplicate comments observed
     * @param totalCommentsObserved  Total comments evaluated in sample window
     * @param commentsInTenMinutes   Burst activity: comments posted within a 10-minute window
     */
    public AccountActivity(double averageLikes, double averageComments, int repeatedComments,
                           int totalCommentsObserved, int commentsInTenMinutes) {
        setAverageLikes(averageLikes);
        setAverageComments(averageComments);
        setRepeatedComments(repeatedComments);
        setTotalCommentsObserved(totalCommentsObserved);
        setCommentsInTenMinutes(commentsInTenMinutes);
    }

    public double getAverageLikes() {
        return averageLikes;
    }

    public void setAverageLikes(double averageLikes) {
        if (averageLikes < 0) {
            throw new IllegalArgumentException("Average likes cannot be negative: " + averageLikes);
        }
        this.averageLikes = averageLikes;
    }

    public double getAverageComments() {
        return averageComments;
    }

    public void setAverageComments(double averageComments) {
        if (averageComments < 0) {
            throw new IllegalArgumentException("Average comments cannot be negative: " + averageComments);
        }
        this.averageComments = averageComments;
    }

    public int getRepeatedComments() {
        return repeatedComments;
    }

    public void setRepeatedComments(int repeatedComments) {
        if (repeatedComments < 0) {
            throw new IllegalArgumentException("Repeated comments cannot be negative: " + repeatedComments);
        }
        this.repeatedComments = repeatedComments;
    }

    public int getTotalCommentsObserved() {
        return totalCommentsObserved;
    }

    public void setTotalCommentsObserved(int totalCommentsObserved) {
        if (totalCommentsObserved < 0) {
            throw new IllegalArgumentException("Total comments observed cannot be negative: " + totalCommentsObserved);
        }
        this.totalCommentsObserved = totalCommentsObserved;
    }

    public int getCommentsInTenMinutes() {
        return commentsInTenMinutes;
    }

    public void setCommentsInTenMinutes(int commentsInTenMinutes) {
        if (commentsInTenMinutes < 0) {
            throw new IllegalArgumentException("Comments in 10 minutes cannot be negative: " + commentsInTenMinutes);
        }
        this.commentsInTenMinutes = commentsInTenMinutes;
    }

    /**
     * Computes the ratio of repeated comments to total comments observed.
     * Returns 0.0 if no comments have been observed.
     *
     * @return ratio between 0.0 and 1.0
     */
    public double getRepetitiveCommentRatio() {
        if (totalCommentsObserved <= 0) {
            return 0.0;
        }
        return (double) repeatedComments / totalCommentsObserved;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AccountActivity that = (AccountActivity) o;
        return Double.compare(that.averageLikes, averageLikes) == 0 &&
               Double.compare(that.averageComments, averageComments) == 0 &&
               repeatedComments == that.repeatedComments &&
               totalCommentsObserved == that.totalCommentsObserved &&
               commentsInTenMinutes == that.commentsInTenMinutes;
    }

    @Override
    public int hashCode() {
        return Objects.hash(averageLikes, averageComments, repeatedComments,
                            totalCommentsObserved, commentsInTenMinutes);
    }

    @Override
    public String toString() {
        return String.format("AvgLikes=%.1f, AvgComments=%.1f, RepComments=%d/%d, Rapid10m=%d",
                averageLikes, averageComments, repeatedComments, totalCommentsObserved, commentsInTenMinutes);
    }
}
