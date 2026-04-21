package com.fluently.feedbackservice.services;

import com.fluently.feedbackservice.dto.FeedbackRequest;
import com.fluently.feedbackservice.dto.FeedbackResponse;
import com.fluently.feedbackservice.dto.ModerationResult;
import com.fluently.feedbackservice.dto.ReactionRequest;
import com.fluently.feedbackservice.entities.Feedback;
import com.fluently.feedbackservice.entities.FeedbackReaction;
import com.fluently.feedbackservice.entities.FlagStatus;
import com.fluently.feedbackservice.entities.FlaggedFeedback;
import com.fluently.feedbackservice.repositories.FeedbackReactionRepository;
import com.fluently.feedbackservice.repositories.FeedbackRepository;
import com.fluently.feedbackservice.repositories.FlaggedFeedbackRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FeedbackService {

    @Autowired
    private FeedbackRepository feedbackRepository;

    @Autowired
    private FeedbackReactionRepository reactionRepository;

    @Autowired
    private FlaggedFeedbackRepository flaggedFeedbackRepository;

    @Autowired
    private ContentModerationService moderationService;

    @Autowired
    private PatternDetectionService patternDetectionService;

    @Transactional
    public FeedbackResponse submitFeedback(FeedbackRequest request) {

        moderationService.checkCooldown(String.valueOf(request.getStudentId()));

        if (feedbackRepository.existsByStudentIdAndLessonId(
                String.valueOf(request.getStudentId()),
                request.getLessonId())) {
            throw new RuntimeException("Student has already submitted feedback for this lesson");
        }

        ModerationResult moderation = moderationService.moderate(request.getComment());
        
        // Ensure gibberish/non-english slang isn't submitted
        moderationService.validateEnglish(request.getComment());

        Feedback feedback = new Feedback();
        feedback.setStudentId(String.valueOf(request.getStudentId()));
        feedback.setLessonId(request.getLessonId());
        feedback.setCourseId(request.getCourseId());
        feedback.setProfessorId(String.valueOf(request.getProfessorId()));
        feedback.setContent(moderation.getCleanedContent());
        feedback.setStarRating(request.getStarRating());
        feedback.setFlagged(moderation.isFlagged());
        feedback.setVisible(!moderation.isFlagged());

        feedback = feedbackRepository.save(feedback);

        if (moderation.isFlagged()) {
            FlaggedFeedback flag = new FlaggedFeedback();
            flag.setFeedbackId(feedback.getId());
            flag.setStudentId(String.valueOf(request.getStudentId()));
            flag.setOriginalContent(request.getComment());
            flag.setCleanedContent(moderation.getCleanedContent());
            flag.setStatus(FlagStatus.CONFIRMED);
            flaggedFeedbackRepository.save(flag);
        }
        patternDetectionService.analyzeAndNotify(
                feedback.getLessonId(),
                feedback.getProfessorId()
        );

        return FeedbackResponse.fromEntity(feedback);
    }

    public List<FeedbackResponse> getFeedbackForLesson(Long lessonId) {
        return feedbackRepository.findByLessonId(lessonId)
                .stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    public List<FeedbackResponse> getFeedbackForCourse(Long courseId) {
        return feedbackRepository.findByCourseId(courseId)
                .stream()
                .map(FeedbackResponse::fromEntity)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteFeedback(Long id, String studentId) {
        Feedback feedback = feedbackRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + id));

        if (!feedback.getStudentId().equals(studentId)) {
            throw new RuntimeException("Not authorized to delete this feedback");
        }

        feedbackRepository.delete(feedback);
    }

    @Transactional
    public FeedbackResponse addReaction(Long feedbackId, ReactionRequest request) {
        Feedback feedback = feedbackRepository.findById(feedbackId)
                .orElseThrow(() -> new RuntimeException("Feedback not found with id: " + feedbackId));

        if (feedback.getStudentId().equals(String.valueOf(request.getStudentId()))) {
            throw new RuntimeException("You cannot react to your own feedback");
        }

        reactionRepository.findByFeedbackIdAndReactorStudentId(
                        feedbackId,
                        String.valueOf(request.getStudentId()))
                .ifPresent(r -> {
                    throw new RuntimeException("Reaction already exists. Use PUT to change it.");
                });

        FeedbackReaction reaction = new FeedbackReaction();
        reaction.setFeedback(feedback);
        reaction.setReactorStudentId(String.valueOf(request.getStudentId()));
        reaction.setReactionType(request.getReactionType());

        reactionRepository.save(reaction);

        Feedback updated = feedbackRepository.findById(feedbackId).orElseThrow();
        return FeedbackResponse.fromEntity(updated);
    }

    @Transactional
    public FeedbackResponse updateReaction(Long feedbackId, ReactionRequest request) {
        FeedbackReaction reaction = reactionRepository
                .findByFeedbackIdAndReactorStudentId(
                        feedbackId,
                        String.valueOf(request.getStudentId()))
                .orElseThrow(() -> new RuntimeException("No existing reaction found. Use POST to add one."));

        reaction.setReactionType(request.getReactionType());
        reactionRepository.save(reaction);

        Feedback updated = feedbackRepository.findById(feedbackId).orElseThrow();
        return FeedbackResponse.fromEntity(updated);
    }

    @Transactional
    public FeedbackResponse removeReaction(Long feedbackId, String studentId) {
        FeedbackReaction reaction = reactionRepository
                .findByFeedbackIdAndReactorStudentId(feedbackId, studentId)
                .orElseThrow(() -> new RuntimeException("Reaction not found"));

        reactionRepository.delete(reaction);

        Feedback updated = feedbackRepository.findById(feedbackId).orElseThrow();
        return FeedbackResponse.fromEntity(updated);
    }
}