package com.fluently.lessonsservice.dto.progress;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CourseProgressSummaryDTO {
    private Long courseId;

    public Long getCourseId() {
        return courseId;
    }

    public void setCourseId(Long courseId) {
        this.courseId = courseId;
    }

    public long getTotalLessons() {
        return totalLessons;
    }

    public void setTotalLessons(long totalLessons) {
        this.totalLessons = totalLessons;
    }

    public long getCompletedLessons() {
        return completedLessons;
    }

    public void setCompletedLessons(long completedLessons) {
        this.completedLessons = completedLessons;
    }

    public double getCompletionPercent() {
        return completionPercent;
    }

    public void setCompletionPercent(double completionPercent) {
        this.completionPercent = completionPercent;
    }

    public Long getNextLessonId() {
        return nextLessonId;
    }

    public void setNextLessonId(Long nextLessonId) {
        this.nextLessonId = nextLessonId;
    }

    public Integer getUnlockedOrderIndexMax() {
        return unlockedOrderIndexMax;
    }

    public void setUnlockedOrderIndexMax(Integer unlockedOrderIndexMax) {
        this.unlockedOrderIndexMax = unlockedOrderIndexMax;
    }

    private long totalLessons;
    private long completedLessons;

    private double completionPercent;

    // unlocking logic
    private Long nextLessonId;              // next not completed lesson
    private Integer unlockedOrderIndexMax;  // u can access lessons with orderIndex <= this
}