package com.fluently.lessonsservice.dto.progress;

import jakarta.persistence.Column;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class LessonHeartbeatRequest {

    @NotBlank

    @Column(name="course_id", nullable = false)
    private Long courseId;

    @NotNull
    @Min(1)
    private Long deltaSeconds;

    public LessonHeartbeatRequest() {}

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public Long getDeltaSeconds() { return deltaSeconds; }
    public void setDeltaSeconds(Long deltaSeconds) { this.deltaSeconds = deltaSeconds; }
}