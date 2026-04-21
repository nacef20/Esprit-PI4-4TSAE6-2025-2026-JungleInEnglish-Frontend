package com.fluently.feedbackservice.entities;

public enum ReactionType {
    INSIGHTFUL(3),
    HELPFUL(1),
    NEEDS_WORK(-1);

    private final int value;

    ReactionType(int value) {
        this.value = value;
    }

    public int getValue() {
        return value;
    }
}
