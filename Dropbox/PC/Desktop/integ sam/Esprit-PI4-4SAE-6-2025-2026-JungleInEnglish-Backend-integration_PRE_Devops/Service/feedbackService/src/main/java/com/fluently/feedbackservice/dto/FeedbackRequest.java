package com.fluently.feedbackservice.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class FeedbackRequest {

    @NotNull
    private Long lessonId;

    @NotNull
    private Long courseId;

    @NotNull
    private String professorId;

    @NotNull
    private String studentId;

    @NotBlank
    private String comment;

    @NotNull
    @Min(1)
    @Max(5)
    private Integer starRating;

    public FeedbackRequest() {}

    public FeedbackRequest(Long lessonId, Long courseId, String professorId,
                           String studentId, String comment, Integer starRating) {
        this.lessonId = lessonId;
        this.courseId = courseId;
        this.professorId = professorId;
        this.studentId = studentId;
        this.comment = comment;
        this.starRating = starRating;
    }

    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }

    public Long getCourseId() { return courseId; }
    public void setCourseId(Long courseId) { this.courseId = courseId; }

    public String getProfessorId() { return professorId; }
    public void setProfessorId(String professorId) { this.professorId = professorId; }

    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }

    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }

    public Integer getStarRating() { return starRating; }
    public void setStarRating(Integer starRating) { this.starRating = starRating; }
}