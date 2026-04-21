package com.fluently.coursesservice.controllers;

import com.fluently.coursesservice.entities.Course;
import com.fluently.coursesservice.entities.CourseLevel;
import com.fluently.coursesservice.repositories.CourseRepository;
import com.fluently.coursesservice.services.CourseService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    @Autowired
    private CourseService courseService;
    private final CourseRepository courseRepository;
    public CourseController(CourseService courseService, CourseRepository courseRepository) {
        this.courseService = courseService;
        this.courseRepository = courseRepository;
    }
    /**
     * US1: Create course
     * POST /api/courses
     */
    @PostMapping("/addCourse")
    public ResponseEntity<Course> createCourse(@RequestBody Course course) {
        Course created = courseService.createCourse(course);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * US9: Get course by ID (view course details)
     * GET /api/courses/{id}
     */
    @GetMapping("getCourse/{id}")
    public ResponseEntity<Course> getCourse(@PathVariable Long id) {
        Course course = courseService.getCourse(id);
        return ResponseEntity.ok(course);
    }

    /**
     * US8: Get all courses (student views available courses)
     * GET /api/courses
     */
    @GetMapping("/getAllcourses")
    public ResponseEntity<List<Course>> getAllCourses(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) CourseLevel level) {

        List<Course> courses = courseService.getAllCourses(search, level);
        return ResponseEntity.ok(courses);
    }

    /**
     * US2: Update course
     * PUT /api/courses/{id}
     */
    @PutMapping("/updateEntreprise/{id}")
    public ResponseEntity<Course> updateCourse(
            @PathVariable Long id,
            @RequestBody Course course) {
        Course updated = courseService.updateCourse(id, course);
        return ResponseEntity.ok(updated);
    }

    /**
     * US3: Delete course
     * DELETE /api/courses/{id}
     */
    @DeleteMapping("/deleteCourse/{id}")
    public ResponseEntity<Void> deleteCourse(@PathVariable Long id) {
        courseService.deleteCourse(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/by-ids")
    public ResponseEntity<List<Course>> byIds(@RequestBody List<Long> ids) {
        return ResponseEntity.ok(courseRepository.findByIdIn(ids));
    }
}