package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import tn.esprit.quiz_microservice.entities.Attempt;

import java.util.List;

public interface AttemptRepository extends JpaRepository<Attempt, Long> {

    // ── Stats queries ──
    @Query("SELECT a.quiz.id, a.quiz.title, AVG(a.score), AVG(a.totalPoints), COUNT(a), " +
           "SUM(CASE WHEN (a.score * 100.0 / a.totalPoints) >= 50 THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.totalPoints > 0 GROUP BY a.quiz.id, a.quiz.title")
    List<Object[]> getAvgScorePerQuiz();

    @Query("SELECT " +
           "SUM(CASE WHEN (a.score * 100.0 / a.totalPoints) >= 80 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN (a.score * 100.0 / a.totalPoints) >= 60 AND (a.score * 100.0 / a.totalPoints) < 80 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN (a.score * 100.0 / a.totalPoints) >= 50 AND (a.score * 100.0 / a.totalPoints) < 60 THEN 1 ELSE 0 END), " +
           "SUM(CASE WHEN (a.score * 100.0 / a.totalPoints) < 50 THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.totalPoints > 0")
    Object[] getGradeDistribution();

    long countByQuizIdAndStudentId(Long quizId, Long studentId);

    List<Attempt> findByQuizIdAndStudentId(Long quizId, Long studentId);

    Page<Attempt> findByStudentId(Long studentId, Pageable pageable);

    Page<Attempt> findByQuizId(Long quizId, Pageable pageable);

    Page<Attempt> findByQuizIdAndStudentId(Long quizId, Long studentId, Pageable pageable);

    // Search by quiz title
    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Page<Attempt> findByQuizTitleContaining(@Param("title") String title, Pageable pageable);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%')) AND a.studentId = :studentId")
    Page<Attempt> findByQuizTitleContainingAndStudentId(@Param("title") String title, @Param("studentId") Long studentId, Pageable pageable);

    // Search by student name
    @Query("SELECT a FROM Attempt a WHERE LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Attempt> findByStudentNameContaining(@Param("name") String name, Pageable pageable);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%')) AND a.quiz.id = :quizId")
    Page<Attempt> findByStudentNameContainingAndQuizId(@Param("name") String name, @Param("quizId") Long quizId, Pageable pageable);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Attempt> findByQuizTitleAndStudentName(@Param("title") String title, @Param("name") String name, Pageable pageable);

    // ── List queries (no pagination, for PDF export) ──
    List<Attempt> findAllByOrderBySubmittedAtDesc();

    List<Attempt> findByQuizIdOrderBySubmittedAtDesc(Long quizId);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%')) ORDER BY a.submittedAt DESC")
    List<Attempt> findByQuizTitleContainingList(@Param("title") String title);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.submittedAt DESC")
    List<Attempt> findByStudentNameContainingList(@Param("name") String name);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%')) AND a.quiz.id = :quizId ORDER BY a.submittedAt DESC")
    List<Attempt> findByQuizTitleContainingAndQuizIdList(@Param("title") String title, @Param("quizId") Long quizId);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%')) AND a.quiz.id = :quizId ORDER BY a.submittedAt DESC")
    List<Attempt> findByStudentNameContainingAndQuizIdList(@Param("name") String name, @Param("quizId") Long quizId);

    @Query("SELECT a FROM Attempt a WHERE LOWER(a.quiz.title) LIKE LOWER(CONCAT('%', :title, '%')) AND LOWER(a.studentName) LIKE LOWER(CONCAT('%', :name, '%')) ORDER BY a.submittedAt DESC")
    List<Attempt> findByQuizTitleAndStudentNameList(@Param("title") String title, @Param("name") String name);

    // Student stats
    @Query("SELECT COUNT(a), " +
           "AVG(CASE WHEN a.totalPoints > 0 THEN (a.score * 100.0 / a.totalPoints) ELSE 0 END), " +
           "MAX(CASE WHEN a.totalPoints > 0 THEN (a.score * 100.0 / a.totalPoints) ELSE 0 END), " +
           "SUM(CASE WHEN a.totalPoints > 0 AND (a.score * 100.0 / a.totalPoints) >= 50 THEN 1 ELSE 0 END) " +
           "FROM Attempt a WHERE a.studentId = :studentId")
    Object[] getStudentStats(@Param("studentId") Long studentId);
}
