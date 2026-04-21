package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Attempt;
import tn.esprit.quiz_microservice.repositories.AttemptRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AttemptServiceImpl implements IAttemptService {

    private final AttemptRepository attemptRepository;

    @Override
    public Attempt save(Attempt attempt) {
        return attemptRepository.save(attempt);
    }

    @Override
    public List<Attempt> findAll() {
        return attemptRepository.findAll();
    }

    @Override
    public Optional<Attempt> findById(Long id) {
        return attemptRepository.findById(id);
    }

    @Override
    public Attempt update(Attempt attempt) {
        Attempt existing = attemptRepository.findById(attempt.getId())
                .orElseThrow(() -> new RuntimeException("Attempt not found with id: " + attempt.getId()));
        existing.setScore(attempt.getScore());
        existing.setTotalPoints(attempt.getTotalPoints());
        existing.setStartedAt(attempt.getStartedAt());
        existing.setSubmittedAt(attempt.getSubmittedAt());
        existing.setQuiz(attempt.getQuiz());
        existing.setStudentId(attempt.getStudentId());
        existing.setStudentName(attempt.getStudentName());
        return attemptRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        attemptRepository.deleteById(id);
    }

    @Override
    public long countByQuizIdAndStudentId(Long quizId, Long studentId) {
        return attemptRepository.countByQuizIdAndStudentId(quizId, studentId);
    }

    @Override
    public Page<Attempt> findAllPaginated(Pageable pageable) {
        return attemptRepository.findAll(pageable);
    }

    @Override
    public Page<Attempt> findByQuizIdPaginated(Long quizId, Pageable pageable) {
        return attemptRepository.findByQuizId(quizId, pageable);
    }

    @Override
    public Page<Attempt> findByStudentIdPaginated(Long studentId, Pageable pageable) {
        return attemptRepository.findByStudentId(studentId, pageable);
    }

    @Override
    public Page<Attempt> findByQuizIdAndStudentIdPaginated(Long quizId, Long studentId, Pageable pageable) {
        return attemptRepository.findByQuizIdAndStudentId(quizId, studentId, pageable);
    }

    @Override
    public Page<Attempt> findByQuizTitlePaginated(String title, Pageable pageable) {
        return attemptRepository.findByQuizTitleContaining(title, pageable);
    }

    @Override
    public Page<Attempt> findByQuizTitleAndStudentIdPaginated(String title, Long studentId, Pageable pageable) {
        return attemptRepository.findByQuizTitleContainingAndStudentId(title, studentId, pageable);
    }

    @Override
    public Page<Attempt> findByStudentNamePaginated(String name, Pageable pageable) {
        return attemptRepository.findByStudentNameContaining(name, pageable);
    }

    @Override
    public Page<Attempt> findByStudentNameAndQuizIdPaginated(String name, Long quizId, Pageable pageable) {
        return attemptRepository.findByStudentNameContainingAndQuizId(name, quizId, pageable);
    }

    @Override
    public Page<Attempt> findByQuizTitleAndStudentNamePaginated(String title, String name, Pageable pageable) {
        return attemptRepository.findByQuizTitleAndStudentName(title, name, pageable);
    }

    @Override
    public Object[] getStudentStats(Long studentId) {
        return attemptRepository.getStudentStats(studentId);
    }
}
