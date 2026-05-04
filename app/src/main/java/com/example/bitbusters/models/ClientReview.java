package com.example.bitbusters.models;

public class ClientReview {
    private final String reviewerName;
    private final int rating;
    private final String comment;
    private final String relativeTime;

    public ClientReview(String reviewerName, int rating, String comment, String relativeTime) {
        this.reviewerName = reviewerName;
        this.rating = rating;
        this.comment = comment;
        this.relativeTime = relativeTime;
    }

    public String getReviewerName() {
        return reviewerName;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public String getRelativeTime() {
        return relativeTime;
    }
}
