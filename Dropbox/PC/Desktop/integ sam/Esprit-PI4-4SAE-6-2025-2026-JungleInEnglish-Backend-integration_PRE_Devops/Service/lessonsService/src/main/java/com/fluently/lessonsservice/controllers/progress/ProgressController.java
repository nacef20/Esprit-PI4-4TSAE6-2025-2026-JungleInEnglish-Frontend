package com.fluently.lessonsservice.controllers.progress;

import com.fluently.lessonsservice.dto.progress.AttachmentCompleteRequest;
import com.fluently.lessonsservice.dto.progress.CourseCompletionRateDTO;
import com.fluently.lessonsservice.dto.progress.CourseProgressSummaryDTO;
import com.fluently.lessonsservice.dto.progress.LessonCompleteRequest;
import com.fluently.lessonsservice.dto.progress.LessonDifficultyDTO;
import com.fluently.lessonsservice.dto.progress.LessonHeartbeatRequest;
import com.fluently.lessonsservice.dto.progress.LessonStartRequest;
import com.fluently.lessonsservice.services.progress.IProgressService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons/progress")
public class ProgressController {

    @Autowired
    private IProgressService progressService;

    @PostMapping("/lesson/start")
    public ResponseEntity<Void> startLesson(@RequestBody LessonStartRequest req,
                                            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        progressService.startLesson(userId, req.getLessonId());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/lesson/complete")
    public ResponseEntity<Void> completeLesson(@RequestBody LessonCompleteRequest req,
                                               @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        progressService.completeLesson(userId, req.getLessonId(), req.getTimeSpentSeconds());
        return ResponseEntity.ok().build();
    }

    @PostMapping("/attachment/complete")
    public ResponseEntity<Void> completeAttachment(@RequestBody AttachmentCompleteRequest req,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        progressService.completeAttachment(userId, req.getAttachmentId());
        return ResponseEntity.ok().build();
    }

    @GetMapping("/course/{courseId}/summary")
    public ResponseEntity<CourseProgressSummaryDTO> summary(@PathVariable Long courseId,
                                                            @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(progressService.getCourseSummary(userId, courseId));
    }

    @PostMapping("/lessons/{lessonId}/heartbeat")
    public ResponseEntity<Void> heartbeat(@PathVariable Long lessonId,
                                          @Valid @RequestBody LessonHeartbeatRequest request,
                                          @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();

        progressService.recordLessonHeartbeat(
                lessonId,
                userId,
                request.getCourseId(),
                request.getDeltaSeconds()
        );

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/course/{courseId}/difficulty")
    public ResponseEntity<List<LessonDifficultyDTO>> difficulty(@PathVariable Long courseId) {
        return ResponseEntity.ok(progressService.getLessonDifficultyByCourse(courseId));
    }

    @GetMapping("/course-completion-rates")
    public ResponseEntity<List<CourseCompletionRateDTO>> completionRates() {
        return ResponseEntity.ok(progressService.getCourseCompletionRates());
    }

    @PostMapping("/course/{courseId}/enroll")
    public ResponseEntity<Void> enroll(@PathVariable Long courseId,
                                       @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        progressService.enrollCourse(userId, courseId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/my-courses")
    public ResponseEntity<List<Long>> myCourses(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(progressService.getMyCourseIds(userId));
    }

    @GetMapping("/recommendations/top3")
    public ResponseEntity<List<Long>> top3Recommendations(@AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getSubject();
        return ResponseEntity.ok(progressService.getTop3RecommendedCourseIds(userId));
    }
}