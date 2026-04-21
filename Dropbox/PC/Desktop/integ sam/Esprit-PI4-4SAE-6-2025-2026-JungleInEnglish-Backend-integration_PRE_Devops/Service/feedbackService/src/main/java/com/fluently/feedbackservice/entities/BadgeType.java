package com.fluently.feedbackservice.entities;

public enum BadgeType {
    NONE(0),
    ROOKIE(10),
    BRONZE(50),
    SILVER(150),
    GOLD(300),
    PLATINUM(500),
    LEGEND(1000);

    private final int threshold;

    BadgeType(int threshold) {
        this.threshold = threshold;
    }

    public int getThreshold() {
        return threshold;
    }

    public static BadgeType calculateBadge(int score) {
        BadgeType currentBadge = NONE;
        for (BadgeType badge : values()) {
            if (score >= badge.getThreshold()) {
                currentBadge = badge;
            }
        }
        return currentBadge;
    }
}
