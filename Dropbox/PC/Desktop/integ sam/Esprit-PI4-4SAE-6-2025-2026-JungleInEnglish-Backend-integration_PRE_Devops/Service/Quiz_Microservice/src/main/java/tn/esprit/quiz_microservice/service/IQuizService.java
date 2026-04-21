package tn.esprit.quiz_microservice.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import tn.esprit.quiz_microservice.entities.Quiz;

import java.util.List;
import java.util.Optional;

public interface IQuizService {

    Quiz save(Quiz quiz);

    List<Quiz> findAll();

    Optional<Quiz> findById(Long id);

    Quiz update(Quiz quiz);

    void deleteById(Long id);

    Page<Quiz> findAllPaginated(Pageable pageable);

    Page<Quiz> findAvailablePaginated(Pageable pageable);

    Page<Quiz> searchByTitle(String title, Pageable pageable);

    Page<Quiz> searchAvailableByTitle(String title, Pageable pageable);

    Page<Quiz> findByStatus(tn.esprit.quiz_microservice.entities.QuizStatus status, Pageable pageable);

    Page<Quiz> searchByTitleAndStatus(String title, tn.esprit.quiz_microservice.entities.QuizStatus status, Pageable pageable);

    Page<Quiz> findOpenForStudent(Long studentId, Pageable pageable);

    Page<Quiz> searchOpenForStudentByTitle(String title, Long studentId, Pageable pageable);

    Page<Quiz> findAttemptedByStudent(Long studentId, Pageable pageable);

    Page<Quiz> searchAttemptedByStudentByTitle(String title, Long studentId, Pageable pageable);
}
