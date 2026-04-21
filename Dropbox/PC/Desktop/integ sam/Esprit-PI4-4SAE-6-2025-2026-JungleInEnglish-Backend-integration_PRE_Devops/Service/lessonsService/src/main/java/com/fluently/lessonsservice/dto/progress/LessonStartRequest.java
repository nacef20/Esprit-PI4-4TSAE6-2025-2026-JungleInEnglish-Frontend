package com.fluently.lessonsservice.dto.progress;

import lombok.Data;

@Data
public class LessonStartRequest {

    private Long lessonId;


    public Long getLessonId() { return lessonId; }
    public void setLessonId(Long lessonId) { this.lessonId = lessonId; }
}