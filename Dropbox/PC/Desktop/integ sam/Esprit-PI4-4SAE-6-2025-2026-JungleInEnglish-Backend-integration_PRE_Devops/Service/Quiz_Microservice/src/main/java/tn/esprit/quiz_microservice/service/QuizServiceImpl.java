package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Quiz;
import tn.esprit.quiz_microservice.entities.QuizStatus;
import tn.esprit.quiz_microservice.repositories.QuizRepository;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuizServiceImpl implements IQuizService {

    private final QuizRepository quizRepository;

    @Override
    public Quiz save(Quiz quiz) {
        return quizRepository.save(quiz);
    }

    @Override
    public List<Quiz> findAll() {
        return quizRepository.findAll();
    }

    @Override
    public Optional<Quiz> findById(Long id) {
        return quizRepository.findById(id);
    }

    @Override
    public Quiz update(Quiz quiz) {
        Quiz existing = quizRepository.findById(quiz.getId())
                .orElseThrow(() -> new RuntimeException("Quiz not found with id: " + quiz.getId()));
        existing.setTitle(quiz.getTitle());
        existing.setDescription(quiz.getDescription());
        existing.setDuration(quiz.getDuration());
        existing.setStatus(quiz.getStatus());
        existing.setTeacherId(quiz.getTeacherId());
        existing.setCourseId(quiz.getCourseId());
        return quizRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        quizRepository.deleteById(id);
    }

    @Override
    public Page<Quiz> findAllPaginated(Pageable pageable) {
        return quizRepository.findAll(pageable);
    }

    @Override
    public Page<Quiz> findAvailablePaginated(Pageable pageable) {
        return quizRepository.findByStatusIn(
                Arrays.asList(QuizStatus.PUBLISHED, QuizStatus.CLOSED), pageable);
    }

    @Override
    public Page<Quiz> searchByTitle(String title, Pageable pageable) {
        return quizRepository.findByTitleContainingIgnoreCase(title, pageable);
    }

    @Override
    public Page<Quiz> searchAvailableByTitle(String title, Pageable pageable) {
        return quizRepository.findByTitleContainingIgnoreCaseAndStatusIn(
                title, Arrays.asList(QuizStatus.PUBLISHED, QuizStatus.CLOSED), pageable);
    }

    @Override
    public Page<Quiz> findByStatus(QuizStatus status, Pageable pageable) {
        return quizRepository.findByStatus(status, pageable);
    }

    @Override
    public Page<Quiz> searchByTitleAndStatus(String title, QuizStatus status, Pageable pageable) {
        return quizRepository.findByStatusAndTitleContainingIgnoreCase(status, title, pageable);
    }

    @Override
    public Page<Quiz> findOpenForStudent(Long studentId, Pageable pageable) {
        return quizRepository.findOpenForStudent(studentId, pageable);
    }

    @Override
    public Page<Quiz> searchOpenForStudentByTitle(String title, Long studentId, Pageable pageable) {
        return quizRepository.findOpenForStudentByTitle(studentId, title, pageable);
    }

    @Override
    public Page<Quiz> findAttemptedByStudent(Long studentId, Pageable pageable) {
        return quizRepository.findAttemptedByStudent(studentId, pageable);
    }

    @Override
    public Page<Quiz> searchAttemptedByStudentByTitle(String title, Long studentId, Pageable pageable) {
        return quizRepository.findAttemptedByStudentByTitle(studentId, title, pageable);
    }
}
