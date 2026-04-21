package tn.esprit.eventmodule.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.eventmodule.entity.Feedback;
import tn.esprit.eventmodule.service.FeedbackService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/event-feedbacks")
public class FeedbackController {

    private final FeedbackService feedbackService;

    public FeedbackController(FeedbackService feedbackService) {
        this.feedbackService = feedbackService;
    }

    @PostMapping("/add")
    public ResponseEntity<Feedback> addFeedback(@Valid @RequestBody Feedback feedback) {
        return ResponseEntity.status(HttpStatus.CREATED).body(feedbackService.saveFeedback(feedback));
    }

    @GetMapping("/event/{eventId}")
    public ResponseEntity<List<Feedback>> getFeedbacksByEvent(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getFeedbackByEvent(eventId));
    }

    @GetMapping("/event/{eventId}/stats")
    public ResponseEntity<Map<String, Object>> getSatisfactionStats(@PathVariable Long eventId) {
        return ResponseEntity.ok(feedbackService.getSatisfactionStats(eventId));
    }
}
