package com.fluently.lessonsservice.dto.progress;

public class LessonDifficultyDTO {
    private Long lessonId;
    private Long startedCount;
    private Long completedCount;
    private double completionRate;   // 0..1
    private double avgTimeSeconds;
    private String difficulty;       // EASY / MEDIUM / HARD

    public LessonDifficultyDTO(Long lessonId, Long startedCount, Long completedCount,
                               double completionRate, double avgTimeSeconds, String difficulty) {
        this.lessonId = lessonId;
        this.startedCount = startedCount;
        this.completedCount = completedCount;
        this.completionRate = completionRate;
        this.avgTimeSeconds = avgTimeSeconds;
        this.difficulty = difficulty;
    }

    public Long getLessonId() { return lessonId; }
    public Long getStartedCount() { return startedCount; }
    public Long getCompletedCount() { return completedCount; }
    public double getCompletionRate() { return completionRate; }
    public double getAvgTimeSeconds() { return avgTimeSeconds; }
    public String getDifficulty() { return difficulty; }
}