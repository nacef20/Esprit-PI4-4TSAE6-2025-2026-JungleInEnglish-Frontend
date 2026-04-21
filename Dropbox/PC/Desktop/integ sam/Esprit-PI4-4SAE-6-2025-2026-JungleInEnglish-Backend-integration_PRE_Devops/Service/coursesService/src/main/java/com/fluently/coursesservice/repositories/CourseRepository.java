package com.fluently.coursesservice.repositories;

import com.fluently.coursesservice.entities.Course;
import com.fluently.coursesservice.entities.CourseLevel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {
    boolean existsByNameIgnoreCase(String name);
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    List<Course> findByIdIn(List<Long> ids);

    @Query("""
        SELECT c FROM Course c
        WHERE (:level IS NULL OR c.level = :level)
          AND (
            :search IS NULL OR :search = '' OR
            LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) OR
            LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))
          )
        ORDER BY c.id DESC
    """)
    List<Course> findFiltered(String search, CourseLevel level);
}