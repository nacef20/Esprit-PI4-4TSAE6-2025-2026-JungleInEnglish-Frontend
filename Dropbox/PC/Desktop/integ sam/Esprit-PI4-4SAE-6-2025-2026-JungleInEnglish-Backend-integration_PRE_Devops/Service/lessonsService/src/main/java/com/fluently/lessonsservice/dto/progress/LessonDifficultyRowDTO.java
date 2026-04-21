package com.fluently.lessonsservice.dto.progress;

public class LessonDifficultyRowDTO {

    private Long lessonId;
    private Long startedCount;
    private Long completedCount;
    private Double avgTimeSeconds;

    public LessonDifficultyRowDTO(Long lessonId, Long startedCount, Long completedCount, Double avgTimeSeconds) {
        this.lessonId = lessonId;
        this.startedCount = startedCount;
        this.completedCount = completedCount;
        this.avgTimeSeconds = avgTimeSeconds;
    }

    public Long getLessonId() { return lessonId; }
    public Long getStartedCount() { return startedCount; }
    public Long getCompletedCount() { return completedCount; }
    public Double getAvgTimeSeconds() { return avgTimeSeconds; }

    public double getCompletionRate() {
        if (startedCount == null || startedCount == 0) return 0.0;
        return (completedCount == null ? 0.0 : completedCount.doubleValue()) / startedCount.doubleValue();
    }
}