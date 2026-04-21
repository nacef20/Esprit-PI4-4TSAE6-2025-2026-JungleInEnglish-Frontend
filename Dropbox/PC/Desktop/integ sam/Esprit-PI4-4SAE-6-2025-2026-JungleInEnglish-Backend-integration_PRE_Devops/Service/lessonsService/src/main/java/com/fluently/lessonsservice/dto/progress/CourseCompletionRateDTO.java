package com.fluently.lessonsservice.dto.progress;


public record CourseCompletionRateDTO(
        Long courseId,
        long startedCount,
        long completedCount,
        double completionRate
) {
    @Override
    public Long courseId() {
        return courseId;
    }

    @Override
    public long startedCount() {
        return startedCount;
    }

    @Override
    public long completedCount() {
        return completedCount;
    }

    @Override
    public double completionRate() {
        return completionRate;
    }
}