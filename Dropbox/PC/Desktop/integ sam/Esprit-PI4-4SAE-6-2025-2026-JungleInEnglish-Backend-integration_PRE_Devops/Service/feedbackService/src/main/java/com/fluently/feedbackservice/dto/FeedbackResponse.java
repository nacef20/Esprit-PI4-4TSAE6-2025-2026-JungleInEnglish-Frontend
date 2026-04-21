package com.fluently.feedbackservice.dto;

import com.fluently.feedbackservice.entities.Feedback;

import java.time.LocalDateTime;

public class FeedbackResponse {

    private Long id;
    private String studentId;
    private Long lessonId;
    private Long courseId;
    private String comment;
    private Integer starRating;
    private LocalDateTime createdAt;
    private int qualityScore;
    private long insightfulCount;
    private long helpfulCount;
    private long needsWorkCount;

    public FeedbackResponse() {}

    public FeedbackResponse(Long id, String studentId, Long lessonId, Long courseId,
                            String comment, Integer starRating, LocalDateTime createdAt,
                            int qualityScore, long insightfulCount, long helpfulCount,
                            long needsWorkCount) {
        this.id = id;
        this.studentId = studentId;
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.comment = comment;
        this.starRating = starRating;
        this.createdAt = createdAt;
        this.qualityScore = qualityScore;
        this.insightfulCount = insightfulCount;
        this.helpfulCount = helpfulCount;
        this.needsWorkCount = needsWorkCount;
    }

    public static FeedbackResponse fromEntity(Feedback feedback) {
        int score = 0;
        long insightful = 0;
        long helpful = 0;
        long needsWork = 0;

        if (feedback.getReactions() != null) {
            for (var r : feedback.getReactions()) {
                score += r.getReactionType().getValue();
                switch (r.getReactionType()) {
                    case INSIGHTFUL -> insightful++;
                    case HELPFUL    -> helpful++;
                    case NEEDS_WORK -> needsWork++;
                }
            }
        }

        FeedbackResponse response = new FeedbackResponse();
        response.setId(feedback.getId());
        response.setStudentId(feedback.getStudentId());
        response.setLessonId(feedback.getLessonId());
        response.setCourseId(feedback.getCourseId());
        response.setComment(feedback.getContent());
        response.setStarRating(feedback.getStarRating());
        response.setCreatedAt(feedback.getCreatedAt());
        response.setQualityScore(score);
        response.setInsightfulCount(insightful);
        response.setHelpfulCount(helpful);
        response.setNeedsWorkCount(needsWork);
        return response;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public int getQualityScore() { return qualityScore; }
    public void setQualityScore(int qualityScore) { this.qualityScore = qualityScore; }

    public long getInsightfulCount() { return insightfulCount; }
    public void setInsightfulCount(long insightfulCount) { this.insightfulCount = insightfulCount; }

    public long getHelpfulCount() { return helpfulCount; }
    public void setHelpfulCount(long helpfulCount) { this.helpfulCount = helpfulCount; }

    public long getNeedsWorkCount() { return needsWorkCount; }
    public void setNeedsWorkCount(long needsWorkCount) { this.needsWorkCount = needsWorkCount; }
}