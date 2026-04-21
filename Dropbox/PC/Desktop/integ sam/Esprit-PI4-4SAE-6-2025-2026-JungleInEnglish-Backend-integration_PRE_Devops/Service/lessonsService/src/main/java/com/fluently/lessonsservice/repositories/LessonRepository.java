package com.fluently.lessonsservice.repositories;


import com.fluently.lessonsservice.entities.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.Optional;

@Repository
public interface LessonRepository extends JpaRepository<Lesson, Long> {

List<Lesson> findByCourseIdOrderByOrderIndexAsc(Long courseId);
    boolean existsByCourseIdAndOrderIndex(Long courseId, Integer orderIndex);
    boolean existsByCourseIdAndTitleIgnoreCase(Long courseId, String title);

    boolean existsByCourseIdAndOrderIndexAndIdNot(Long courseId, Integer orderIndex, Long id);
    boolean existsByCourseIdAndTitleIgnoreCaseAndIdNot(Long courseId, String title, Long id);

    int countByCourseId(Long courseId);
    Optional<Lesson> findFirstByCourseIdOrderByOrderIndexAsc(Long courseId);

    @Query("select max(l.orderIndex) from Lesson l where l.courseId = :courseId")
    Integer findMaxOrderIndexByCourse(@Param("courseId") Long courseId);

    // smallest orderIndex not completed
    @Query("select l from Lesson l " +
            "where l.courseId = :courseId " +
            "and l.id not in :completedIds " +
            "and l.orderIndex = (" +
            "   select min(l2.orderIndex) from Lesson l2 " +
            "   where l2.courseId = :courseId " +
            "   and l2.id not in :completedIds" +
            ")")
    Optional<Lesson> findNextNotCompleted(@Param("courseId") Long courseId,
                                          @Param("completedIds") List<Long> completedIds);
}