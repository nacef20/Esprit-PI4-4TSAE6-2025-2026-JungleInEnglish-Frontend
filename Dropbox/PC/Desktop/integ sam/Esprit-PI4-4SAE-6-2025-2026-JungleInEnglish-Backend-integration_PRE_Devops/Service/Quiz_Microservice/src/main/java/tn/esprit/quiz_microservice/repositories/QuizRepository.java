package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.quiz_microservice.entities.Quiz;
import tn.esprit.quiz_microservice.entities.QuizStatus;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Long> {

    Page<Quiz> findByStatusIn(List<QuizStatus> statuses, Pageable pageable);

    Page<Quiz> findByTitleContainingIgnoreCase(String title, Pageable pageable);

    Page<Quiz> findByTitleContainingIgnoreCaseAndStatusIn(String title, List<QuizStatus> statuses, Pageable pageable);

    // Back-office: filter by single status
    Page<Quiz> findByStatus(QuizStatus status, Pageable pageable);

    Page<Quiz> findByStatusAndTitleContainingIgnoreCase(QuizStatus status, String title, Pageable pageable);

    // Front-office: open quizzes (PUBLISHED + not attempted by student)
    @Query("SELECT q FROM Quiz q WHERE q.status = 'PUBLISHED' AND q.id NOT IN (SELECT DISTINCT a.quiz.id FROM Attempt a WHERE a.studentId = :studentId)")
    Page<Quiz> findOpenForStudent(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT q FROM Quiz q WHERE q.status = 'PUBLISHED' AND q.id NOT IN (SELECT DISTINCT a.quiz.id FROM Attempt a WHERE a.studentId = :studentId) AND LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Quiz> findOpenForStudentByTitle(@Param("studentId") Long studentId, @Param("title") String title, Pageable pageable);

    // Front-office: already attempted by student
    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.attempts a WHERE a.studentId = :studentId AND q.status IN ('PUBLISHED', 'CLOSED')")
    Page<Quiz> findAttemptedByStudent(@Param("studentId") Long studentId, Pageable pageable);

    @Query("SELECT DISTINCT q FROM Quiz q JOIN q.attempts a WHERE a.studentId = :studentId AND q.status IN ('PUBLISHED', 'CLOSED') AND LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Quiz> findAttemptedByStudentByTitle(@Param("studentId") Long studentId, @Param("title") String title, Pageable pageable);
}
