package com.fluently.feedbackservice.controllers;

import com.fluently.feedbackservice.dto.FeedbackRequest;
import com.fluently.feedbackservice.dto.FeedbackResponse;
import com.fluently.feedbackservice.dto.ReactionRequest;
import com.fluently.feedbackservice.services.FeedbackService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/feedbacks")
public class FeedbackController {

    @Autowired
    private FeedbackService feedbackService;

    // Create feedback
    @PostMapping("/addFeedback")
    public ResponseEntity<FeedbackResponse> createFeedback(@RequestBody FeedbackRequest request) {
        FeedbackResponse created = feedbackService.submitFeedback(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get feedbacks by lesson
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackForLesson(@PathVariable Long lessonId) {
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbackForLesson(lessonId);
        return ResponseEntity.ok(feedbacks);
    }

    // Get feedbacks by course
    @GetMapping("/course/{courseId}")
    public ResponseEntity<List<FeedbackResponse>> getFeedbackForCourse(@PathVariable Long courseId) {
        List<FeedbackResponse> feedbacks = feedbackService.getFeedbackForCourse(courseId);
        return ResponseEntity.ok(feedbacks);
    }

    // Delete feedback
    @DeleteMapping("/deleteFeedback/{id}")
    public ResponseEntity<Void> deleteFeedback(@PathVariable Long id, @RequestParam String studentId) {
        feedbackService.deleteFeedback(id, studentId);
        return ResponseEntity.noContent().build();
    }

    // Add reaction
    @PostMapping("/{feedbackId}/addReaction")
    public ResponseEntity<FeedbackResponse> addReaction(
            @PathVariable Long feedbackId, 
            @RequestBody ReactionRequest request) {
        FeedbackResponse updated = feedbackService.addReaction(feedbackId, request);
        return ResponseEntity.ok(updated);
    }

    // Update reaction
    @PutMapping("/{feedbackId}/updateReaction")
    public ResponseEntity<FeedbackResponse> updateReaction(
            @PathVariable Long feedbackId, 
            @RequestBody ReactionRequest request) {
        FeedbackResponse updated = feedbackService.updateReaction(feedbackId, request);
        return ResponseEntity.ok(updated);
    }

    // Remove reaction
    @DeleteMapping("/{feedbackId}/deleteReaction")
    public ResponseEntity<FeedbackResponse> removeReaction(
            @PathVariable Long feedbackId, 
            @RequestParam String studentId) {
        FeedbackResponse updated = feedbackService.removeReaction(feedbackId, studentId);
        return ResponseEntity.ok(updated);
    }
}
