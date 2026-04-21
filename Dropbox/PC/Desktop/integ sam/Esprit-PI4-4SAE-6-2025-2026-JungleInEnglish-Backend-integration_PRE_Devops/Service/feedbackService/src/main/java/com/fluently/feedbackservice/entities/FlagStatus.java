package com.fluently.feedbackservice.entities;

public enum FlagStatus {
    PENDING,    // awaiting review if i wanna add admin role
    DISMISSED,  // false positive
    CONFIRMED,  // violation
    EXPIRED     // cooldown lifted automatically by scheduler
}
