package com.fluently.feedbackservice.services;

import com.fluently.feedbackservice.dto.FeedbackRequest;
import com.fluently.feedbackservice.dto.FeedbackResponse;
import com.fluently.feedbackservice.dto.ModerationResult;
import com.fluently.feedbackservice.dto.ReactionRequest;
import com.fluently.feedbackservice.entities.Feedback;
import com.fluently.feedbackservice.entities.FeedbackReaction;
import com.fluently.feedbackservice.entities.ReactionType;
import com.fluently.feedbackservice.repositories.FeedbackReactionRepository;
import com.fluently.feedbackservice.repositories.FeedbackRepository;
import com.fluently.feedbackservice.repositories.FlaggedFeedbackRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class FeedbackServiceTest {

    @Mock
    private FeedbackRepository feedbackRepository;

    @Mock
    private FeedbackReactionRepository reactionRepository;

    @Mock
    private FlaggedFeedbackRepository flaggedFeedbackRepository;

    @Mock
    private ContentModerationService moderationService;

    @Mock
    private PatternDetectionService patternDetectionService;

    // Inject the mocked dependencies into the service
    @InjectMocks
    private FeedbackService feedbackService;

    @Test
    @DisplayName("Should successfully submit feedback when it is valid and not flagged")
    void submitFeedback_Success() {
        // 1. Arrange (Set up mocks and input data)
        FeedbackRequest request = new FeedbackRequest(
                1L,        // lessonId
                2L,        // courseId
                "prof123", // professorId
                "stud456", // studentId
                "Great lesson!", // comment
                5          // starRating
        );

        // standard execution
        doNothing().when(moderationService).checkCooldown(request.getStudentId());

        // Mock that the student hasn't submitted feedback yet
        when(feedbackRepository.existsByStudentIdAndLessonId("stud456", 1L)).thenReturn(false);

        // Mock moderation result to return clean text and not flagged
        ModerationResult moderationResult = new ModerationResult(false, "Great lesson!");
        when(moderationService.moderate(request.getComment())).thenReturn(moderationResult);

        // what the repo will return
        Feedback savedFeedback = new Feedback();
        savedFeedback.setId(100L);
        savedFeedback.setStudentId("stud456");
        savedFeedback.setLessonId(1L);
        savedFeedback.setCourseId(2L);
        savedFeedback.setProfessorId("prof123");
        savedFeedback.setContent("Great lesson!");
        savedFeedback.setStarRating(5);
        savedFeedback.setFlagged(false);
        savedFeedback.setVisible(true);

        when(feedbackRepository.save(any(Feedback.class))).thenReturn(savedFeedback);

        // Mock pattern detection (to not get exceptions)
        doNothing().when(patternDetectionService).analyzeAndNotify(1L, "prof123");

        // 2. Act (Call the method being tested)
        FeedbackResponse response = feedbackService.submitFeedback(request);

        // 3. Verify
        assertNotNull(response, "Response should not be null");
        assertEquals(100L, response.getId(), "Feedback ID should match");
        assertEquals("stud456", response.getStudentId(), "Student ID should match");
        assertEquals("Great lesson!", response.getComment(), "Content should match");

        // Verify that expected methods are called once
        verify(moderationService, times(1)).checkCooldown("stud456");
        verify(feedbackRepository, times(1)).existsByStudentIdAndLessonId("stud456", 1L);
        verify(moderationService, times(1)).moderate("Great lesson!");

        // capture what was passed into feedbackRepository.save()
        ArgumentCaptor<Feedback> feedbackCaptor = ArgumentCaptor.forClass(Feedback.class);
        verify(feedbackRepository, times(1)).save(feedbackCaptor.capture());

        // Verify the properties of the entity that was passed to save()
        Feedback capturedFeedback = feedbackCaptor.getValue();
        assertEquals("stud456", capturedFeedback.getStudentId());
        assertEquals(1L, capturedFeedback.getLessonId());
        assertEquals(2L, capturedFeedback.getCourseId());
        assertEquals("prof123", capturedFeedback.getProfessorId());
        assertEquals("Great lesson!", capturedFeedback.getContent());
        assertEquals(5, capturedFeedback.getStarRating());
        assertFalse(capturedFeedback.isFlagged());
        assertTrue(capturedFeedback.isVisible());

        // Verify other service outcomes
        verify(patternDetectionService, times(1)).analyzeAndNotify(1L, "prof123");
        verify(flaggedFeedbackRepository, never()).save(any()); // Assuming FlaggedFeedback class handles flagged items
    }

    @Test
    @DisplayName("Should throw an exception when the student has already submitted feedback for the lesson")
    void submitFeedback_Failure_AlreadySubmitted() {
        // 1. Arrange
        FeedbackRequest request = new FeedbackRequest(
                1L,        // lessonId
                2L,        // courseId
                "prof123", // professorId
                "stud456", // studentId
                "Great lesson!", // comment
                5          // starRating
        );

        doNothing().when(moderationService).checkCooldown(request.getStudentId());

        // the trap : mock the duplicate check indicating a feedback already exists
        when(feedbackRepository.existsByStudentIdAndLessonId("stud456", 1L)).thenReturn(true);

        // 2. Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            feedbackService.submitFeedback(request);
        });

        // Ensure the exception message is exact
        assertEquals("Student has already submitted feedback for this lesson", exception.getMessage());

        // Verify interactions that should have successfully happened
        verify(moderationService, times(1)).checkCooldown("stud456");
        verify(feedbackRepository, times(1)).existsByStudentIdAndLessonId("stud456", 1L);

        // Ensure interactions that should NOT occur aren't called
        verify(moderationService, never()).moderate(anyString());
        verify(feedbackRepository, never()).save(any(Feedback.class));
        verify(patternDetectionService, never()).analyzeAndNotify(anyLong(), anyString());
        verify(flaggedFeedbackRepository, never()).save(any());
    }

    // ─── READ ───────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("getFeedbackForLesson - returns all feedbacks mapped to DTO")
    void getFeedbackForLesson_ReturnsList() {
        Feedback f = makeFeedback(1L, "stud1", 10L);
        when(feedbackRepository.findByLessonId(10L)).thenReturn(List.of(f));

        List<FeedbackResponse> result = feedbackService.getFeedbackForLesson(10L);

        assertEquals(1, result.size());
        assertEquals("stud1", result.get(0).getStudentId());
        verify(feedbackRepository).findByLessonId(10L);
    }

    @Test
    @DisplayName("getFeedbackForCourse - returns all feedbacks mapped to DTO")
    void getFeedbackForCourse_ReturnsList() {
        Feedback f = makeFeedback(2L, "stud2", 10L);
        when(feedbackRepository.findByCourseId(5L)).thenReturn(List.of(f));

        List<FeedbackResponse> result = feedbackService.getFeedbackForCourse(5L);

        assertEquals(1, result.size());
        verify(feedbackRepository).findByCourseId(5L);
    }

    // ─── DELETE ─────────────────────────────────────────────────────────────────

    @Test
    @DisplayName("deleteFeedback - owner can delete their feedback")
    void deleteFeedback_Success() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        feedbackService.deleteFeedback(10L, "stud1");

        verify(feedbackRepository).delete(f);
    }

    @Test
    @DisplayName("deleteFeedback - throws when student is not the owner")
    void deleteFeedback_NotAuthorized() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.deleteFeedback(10L, "otherStudent"));

        assertEquals("Not authorized to delete this feedback", ex.getMessage());
        verify(feedbackRepository, never()).delete(any());
    }

    @Test
    @DisplayName("deleteFeedback - throws when feedback not found")
    void deleteFeedback_NotFound() {
        when(feedbackRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.deleteFeedback(99L, "stud1"));

        assertTrue(ex.getMessage().contains("Feedback not found"));
    }

    // ─── ADD REACTION ────────────────────────────────────────────────────────────

    @Test
    @DisplayName("addReaction - another student can react to a feedback")
    void addReaction_Success() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));
        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.empty());
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f)); // reload

        ReactionRequest req = new ReactionRequest("stud2", ReactionType.HELPFUL);
        feedbackService.addReaction(10L, req);

        verify(reactionRepository).save(any(FeedbackReaction.class));
    }

    @Test
    @DisplayName("addReaction - throws when student reacts to their own feedback")
    void addReaction_SelfReact_Throws() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        ReactionRequest req = new ReactionRequest("stud1", ReactionType.HELPFUL);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.addReaction(10L, req));

        assertEquals("You cannot react to your own feedback", ex.getMessage());
        verify(reactionRepository, never()).save(any());
    }

    @Test
    @DisplayName("addReaction - throws when reaction already exists")
    void addReaction_Duplicate_Throws() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        FeedbackReaction existing = new FeedbackReaction();
        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.of(existing));

        ReactionRequest req = new ReactionRequest("stud2", ReactionType.INSIGHTFUL);
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.addReaction(10L, req));

        assertTrue(ex.getMessage().contains("Reaction already exists"));
        verify(reactionRepository, never()).save(any());
    }

    // ─── UPDATE REACTION ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("updateReaction - changes reaction type on existing reaction")
    void updateReaction_Success() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        FeedbackReaction existing = new FeedbackReaction();
        existing.setReactionType(ReactionType.HELPFUL);
        existing.setFeedback(f);

        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.of(existing));
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        ReactionRequest req = new ReactionRequest("stud2", ReactionType.NEEDS_WORK);
        feedbackService.updateReaction(10L, req);

        assertEquals(ReactionType.NEEDS_WORK, existing.getReactionType());
        verify(reactionRepository).save(existing);
    }

    @Test
    @DisplayName("updateReaction - throws when no reaction exists yet")
    void updateReaction_NotFound_Throws() {
        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.updateReaction(10L, new ReactionRequest("stud2", ReactionType.HELPFUL)));

        assertTrue(ex.getMessage().contains("No existing reaction found"));
    }

    // ─── REMOVE REACTION ─────────────────────────────────────────────────────────

    @Test
    @DisplayName("removeReaction - deletes the reaction and returns updated feedback")
    void removeReaction_Success() {
        Feedback f = makeFeedback(10L, "stud1", 1L);
        FeedbackReaction reaction = new FeedbackReaction();
        reaction.setFeedback(f);

        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.of(reaction));
        when(feedbackRepository.findById(10L)).thenReturn(Optional.of(f));

        feedbackService.removeReaction(10L, "stud2");

        verify(reactionRepository).delete(reaction);
    }

    @Test
    @DisplayName("removeReaction - throws when reaction not found")
    void removeReaction_NotFound_Throws() {
        when(reactionRepository.findByFeedbackIdAndReactorStudentId(10L, "stud2"))
                .thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> feedbackService.removeReaction(10L, "stud2"));

        assertEquals("Reaction not found", ex.getMessage());
        verify(reactionRepository, never()).delete(any());
    }

    // ─── Helper ──────────────────────────────────────────────────────────────────

    /** Builds a minimal, visible Feedback for reuse across tests. */
    private Feedback makeFeedback(Long id, String studentId, Long lessonId) {
        Feedback f = new Feedback();
        f.setId(id);
        f.setStudentId(studentId);
        f.setLessonId(lessonId);
        f.setCourseId(5L);
        f.setProfessorId("prof123");
        f.setContent("Nice lesson!");
        f.setStarRating(4);
        f.setFlagged(false);
        f.setVisible(true);
        return f;
    }
}
