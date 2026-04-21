package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.quiz_microservice.dto.SubmitQuizRequest;
import tn.esprit.quiz_microservice.entities.*;
import tn.esprit.quiz_microservice.repositories.*;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
public class QuizAttemptService {

    private final AttemptRepository attemptRepository;
    private final AttemptAnswerRepository attemptAnswerRepository;
    private final QuizRepository quizRepository;
    private final QuestionRepository questionRepository;
    private final ChoiceRepository choiceRepository;

    /**
     * Start a quiz attempt. Records startedAt = now on the server.
     * If an in-progress attempt exists (submittedAt == null), returns it.
     * If a completed attempt exists, throws exception.
     */
    @Transactional
    public Attempt startAttempt(Long quizId, Long studentId, String studentName) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new RuntimeException("Quiz not found: " + quizId));

        if (quiz.getStatus() == QuizStatus.CLOSED) {
            throw new RuntimeException("QUIZ_CLOSED");
        }

        // Check existing attempts
        List<Attempt> existing = attemptRepository.findByQuizIdAndStudentId(quizId, studentId);
        for (Attempt a : existing) {
            if (a.getSubmittedAt() != null) {
                // Already completed
                throw new RuntimeException("ALREADY_COMPLETED");
            }
            // In-progress attempt found — check if it's expired
            if (isExpired(a, quiz)) {
                // Auto-submit with 0 score
                autoSubmitExpired(a, quiz);
                throw new RuntimeException("ALREADY_COMPLETED");
            }
            // Still in progress and time remaining — return it
            return a;
        }

        // Create new attempt
        Attempt attempt = new Attempt();
        attempt.setQuiz(quiz);
        attempt.setStudentId(studentId);
        attempt.setStudentName(studentName);
        attempt.setStartedAt(LocalDateTime.now());
        // submittedAt stays null = in progress
        // score stays null = not scored yet

        return attemptRepository.save(attempt);
    }

    /**
     * Submit a quiz attempt with answers. Server-side time validation + score calculation.
     * If time has expired, the attempt is still saved but flagged.
     */
    @Transactional
    public Map<String, Object> submitAttempt(Long attemptId, SubmitQuizRequest request) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (attempt.getSubmittedAt() != null) {
            throw new RuntimeException("ALREADY_SUBMITTED");
        }

        Quiz quiz = attempt.getQuiz();
        if (quiz == null) {
            throw new RuntimeException("Quiz not found for attempt");
        }

        boolean timeExpired = isExpired(attempt, quiz);

        // Load questions and calculate score
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        int totalPoints = questions.stream().mapToInt(Question::getPoints).sum();
        float score = 0;

        // Map question -> correct choice
        Map<Long, Choice> correctChoices = new HashMap<>();
        for (Question q : questions) {
            List<Choice> choices = choiceRepository.findByQuestionId(q.getId());
            for (Choice c : choices) {
                if (Boolean.TRUE.equals(c.getIsCorrect())) {
                    correctChoices.put(q.getId(), c);
                }
            }
        }

        // Save answers and calculate score
        List<AttemptAnswer> savedAnswers = new ArrayList<>();
        if (request.getAnswers() != null) {
            for (SubmitQuizRequest.AnswerEntry entry : request.getAnswers()) {
                Question question = questionRepository.findById(entry.getQuestionId()).orElse(null);
                Choice selectedChoice = choiceRepository.findById(entry.getChoiceId()).orElse(null);

                if (question != null && selectedChoice != null) {
                    AttemptAnswer answer = new AttemptAnswer();
                    answer.setAttempt(attempt);
                    answer.setQuestion(question);
                    answer.setSelectedChoice(selectedChoice);
                    savedAnswers.add(answer);

                    // Check if correct
                    if (Boolean.TRUE.equals(selectedChoice.getIsCorrect())) {
                        score += question.getPoints();
                    }
                }
            }
        }

        // Save all answers
        if (!savedAnswers.isEmpty()) {
            attemptAnswerRepository.saveAll(savedAnswers);
        }

        // Update attempt
        attempt.setScore(score);
        attempt.setTotalPoints(totalPoints);
        attempt.setSubmittedAt(LocalDateTime.now());
        attemptRepository.save(attempt);

        // Build response
        Map<String, Object> result = new HashMap<>();
        result.put("attemptId", attempt.getId());
        result.put("score", score);
        result.put("totalPoints", totalPoints);
        result.put("percentage", totalPoints > 0 ? Math.round(score * 100f / totalPoints) : 0);
        result.put("timeExpired", timeExpired);
        result.put("submittedAt", attempt.getSubmittedAt().toString());
        return result;
    }

    /**
     * Get remaining time in seconds for an in-progress attempt.
     * Returns 0 if expired.
     */
    public long getRemainingTime(Long attemptId) {
        Attempt attempt = attemptRepository.findById(attemptId)
                .orElseThrow(() -> new RuntimeException("Attempt not found: " + attemptId));

        if (attempt.getSubmittedAt() != null) {
            return 0; // Already submitted
        }

        Quiz quiz = attempt.getQuiz();
        if (quiz == null || attempt.getStartedAt() == null) {
            return 0;
        }

        LocalDateTime deadline = attempt.getStartedAt().plusMinutes(quiz.getDuration());
        long remaining = Duration.between(LocalDateTime.now(), deadline).getSeconds();
        return Math.max(0, remaining);
    }

    // ─── PRIVATE HELPERS ───

    private boolean isExpired(Attempt attempt, Quiz quiz) {
        if (attempt.getStartedAt() == null || quiz.getDuration() <= 0) return false;
        LocalDateTime deadline = attempt.getStartedAt().plusMinutes(quiz.getDuration());
        return LocalDateTime.now().isAfter(deadline);
    }

    private void autoSubmitExpired(Attempt attempt, Quiz quiz) {
        List<Question> questions = questionRepository.findByQuizId(quiz.getId());
        int totalPoints = questions.stream().mapToInt(Question::getPoints).sum();

        // Check if any answers were already saved
        List<AttemptAnswer> existingAnswers = attemptAnswerRepository.findByAttemptId(attempt.getId());
        float score = 0;
        for (AttemptAnswer a : existingAnswers) {
            if (a.getSelectedChoice() != null && Boolean.TRUE.equals(a.getSelectedChoice().getIsCorrect())) {
                score += a.getQuestion().getPoints();
            }
        }

        attempt.setScore(score);
        attempt.setTotalPoints(totalPoints);
        attempt.setSubmittedAt(attempt.getStartedAt().plusMinutes(quiz.getDuration()));
        attemptRepository.save(attempt);
    }
}
