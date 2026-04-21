package com.fluently.feedbackservice.repositories;

import com.fluently.feedbackservice.entities.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    List<Feedback> findByLessonId(Long lessonId);

    List<Feedback> findByCourseId(Long courseId);

    boolean existsByStudentIdAndLessonId(String studentId, Long lessonId);

    @Query("""
                SELECT COALESCE(SUM(
                    CASE r.reactionType
                        WHEN 'INSIGHTFUL' THEN 3
                        WHEN 'HELPFUL'    THEN 1
                        WHEN 'NEEDS_WORK' THEN -1
                        ELSE 0
                    END
                ), 0)
                FROM Feedback f
                JOIN f.reactions r
                WHERE f.professorId = :professorId
            """)
    int calculateTotalScoreForProfessor(@Param("professorId") String professorId);

    @Query("""
        SELECT COALESCE(AVG(f.starRating), 0.0)
        FROM Feedback f
        WHERE f.professorId = :professorId
    """)
    Double calculateAverageStarRatingForProfessor(@Param("professorId") String professorId);
}