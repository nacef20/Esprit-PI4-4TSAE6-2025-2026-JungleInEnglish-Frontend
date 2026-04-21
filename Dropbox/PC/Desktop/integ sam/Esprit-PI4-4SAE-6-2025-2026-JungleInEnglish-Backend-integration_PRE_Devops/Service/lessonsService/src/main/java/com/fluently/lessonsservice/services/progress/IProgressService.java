package com.fluently.lessonsservice.services.progress;

import com.fluently.lessonsservice.dto.progress.CourseCompletionRateDTO;
import com.fluently.lessonsservice.dto.progress.CourseProgressSummaryDTO;
import com.fluently.lessonsservice.dto.progress.LessonDifficultyDTO;

import java.util.List;

public interface IProgressService {

    void startLesson(String userId, Long lessonId);

    void completeLesson(String userId, Long lessonId, Long timeSpentSeconds);

    void completeAttachment(String userId, Long attachmentId);

    CourseProgressSummaryDTO getCourseSummary(String userId, Long courseId);

    void recordLessonHeartbeat(Long lessonId, String userId, Long courseId, long deltaSeconds);

    List<LessonDifficultyDTO> getLessonDifficultyByCourse(Long courseId);

    List<CourseCompletionRateDTO> getCourseCompletionRates();

    void enrollCourse(String userId, Long courseId);

    List<Long> getMyCourseIds(String userId);

    List<Long> getTop3RecommendedCourseIds(String userId);
}