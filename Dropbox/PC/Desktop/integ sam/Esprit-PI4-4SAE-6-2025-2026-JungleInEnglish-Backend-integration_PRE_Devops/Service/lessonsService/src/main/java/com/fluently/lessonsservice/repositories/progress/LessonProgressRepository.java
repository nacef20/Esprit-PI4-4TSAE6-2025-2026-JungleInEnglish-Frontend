package com.fluently.lessonsservice.repositories.progress;

import com.fluently.lessonsservice.dto.progress.LessonDifficultyRowDTO;
import com.fluently.lessonsservice.entities.progress.LessonProgress;
import com.fluently.lessonsservice.entities.progress.ProgressStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LessonProgressRepository extends JpaRepository<LessonProgress, Long> {

    Optional<LessonProgress> findByUserIdAndLessonIdAndCourseId(String userId, Long lessonId, Long courseId);

    @Query("select count(lp) from LessonProgress lp " +
            "where lp.courseId = :courseId and lp.userId = :userId and lp.status = :status")
    long countByCourseAndUserAndStatus(@Param("courseId") Long courseId,
                                       @Param("userId") String userId,
                                       @Param("status") ProgressStatus status);

    @Query("select lp.lessonId from LessonProgress lp " +
            "where lp.courseId = :courseId and lp.userId = :userId " +
            "and lp.status = com.fluently.lessonsservice.entities.progress.ProgressStatus.COMPLETED")
    List<Long> findCompletedLessonIds(@Param("courseId") Long courseId,
                                      @Param("userId") String userId);

    @Query("select lp from LessonProgress lp " +
            "where lp.courseId = :courseId and lp.userId = :userId " +
            "and lp.status <> com.fluently.lessonsservice.entities.progress.ProgressStatus.COMPLETED " +
            "order by lp.lastActivityAt asc")
    List<LessonProgress> findNonCompletedOrdered(@Param("courseId") Long courseId,
                                                 @Param("userId") String userId);

    @Query("""
        SELECT new com.fluently.lessonsservice.dto.progress.LessonDifficultyRowDTO(
            lp.lessonId,
            COUNT(DISTINCT lp.userId),
            COUNT(DISTINCT CASE WHEN lp.completedAt IS NOT NULL THEN lp.userId ELSE NULL END),
            AVG(lp.timeSpentSeconds)
        )
        FROM LessonProgress lp
        WHERE lp.courseId = :courseId
        GROUP BY lp.lessonId
    """)
    List<LessonDifficultyRowDTO> getDifficultyAggByCourse(@Param("courseId") Long courseId);

    @Query("""
        SELECT lp.courseId,
               COUNT(DISTINCT lp.userId),
               COUNT(DISTINCT CASE
                    WHEN lp.status = com.fluently.lessonsservice.entities.progress.ProgressStatus.COMPLETED
                    THEN lp.userId ELSE NULL END)
        FROM LessonProgress lp
        WHERE lp.status IN (
            com.fluently.lessonsservice.entities.progress.ProgressStatus.IN_PROGRESS,
            com.fluently.lessonsservice.entities.progress.ProgressStatus.COMPLETED
        )
        GROUP BY lp.courseId
    """)
    List<Object[]> getCourseCompletionAgg();

    @Query("""
       select distinct lp.courseId
       from LessonProgress lp
       where lp.userId = :userId
         and lp.status in (
             com.fluently.lessonsservice.entities.progress.ProgressStatus.IN_PROGRESS,
             com.fluently.lessonsservice.entities.progress.ProgressStatus.COMPLETED
         )
    """)
    List<Long> findMyCourseIds(@Param("userId") String userId);
}