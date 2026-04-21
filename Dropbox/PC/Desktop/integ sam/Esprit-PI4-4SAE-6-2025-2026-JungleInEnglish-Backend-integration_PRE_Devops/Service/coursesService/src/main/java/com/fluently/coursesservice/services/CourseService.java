package com.fluently.coursesservice.services;

import com.fluently.coursesservice.entities.Course;
import com.fluently.coursesservice.entities.CourseLevel;
import com.fluently.coursesservice.repositories.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CourseService {

    @Autowired
    private CourseRepository courseRepository;

    public Course createCourse(Course course) {
        if (course.getName() != null) course.setName(course.getName().trim());

        if (courseRepository.existsByNameIgnoreCase(course.getName())) {
            throw new RuntimeException("Course name already exists.");
        }

        return courseRepository.save(course);
    }

    public Course getCourse(Long id) {
        return courseRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Course not found with id: " + id));
    }

    public List<Course> getAllCourses() {
        return courseRepository.findAll();
    }
    public List<Course> getAllCourses(String search, CourseLevel level) {
        if (search != null) search = search.trim();
        return courseRepository.findFiltered(search, level);
    }

    public Course updateCourse(Long id, Course courseDetails) {
        Course course = getCourse(id);

        if (courseDetails.getName() != null) courseDetails.setName(courseDetails.getName().trim());

        if (courseRepository.existsByNameIgnoreCaseAndIdNot(courseDetails.getName(), id)) {
            throw new RuntimeException("Course name already exists.");
        }

        // Update fields
        course.setName(courseDetails.getName());
        course.setDescription(courseDetails.getDescription());
        course.setDuration(courseDetails.getDuration());
        course.setLevel(courseDetails.getLevel());
        course.setImageUrl(courseDetails.getImageUrl());

        return courseRepository.save(course);
    }

    /**
     * US3: Delete a course
     * Teacher removes outdated course
     */
    public void deleteCourse(Long id) {
        Course course = getCourse(id);
        courseRepository.delete(course);
    }
}