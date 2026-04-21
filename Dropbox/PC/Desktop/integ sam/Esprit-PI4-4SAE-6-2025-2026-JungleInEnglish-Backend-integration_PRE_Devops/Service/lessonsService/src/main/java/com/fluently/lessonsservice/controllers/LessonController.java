package com.fluently.lessonsservice.controllers;

import com.fluently.lessonsservice.entities.Lesson;
import com.fluently.lessonsservice.services.ILessonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
public class LessonController {

    @Autowired
    private ILessonService lessonService;

    // Create lesson
    @PostMapping("/addLesson")
    public ResponseEntity<Lesson> createLesson(@RequestBody Lesson lesson) {
        Lesson created = lessonService.addOrUpdateLesson(lesson);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    // Get lesson by ID
    @GetMapping("findLesson/{id}")
    public ResponseEntity<Lesson> getLesson(@PathVariable Long id) {
        Lesson lesson = lessonService.findLessonById(id);
        return ResponseEntity.ok(lesson);
    }

    // Get all lessons
    @GetMapping("/findallLessons")
    public ResponseEntity<List<Lesson>> getAllLessons() {
        List<Lesson> lessons = lessonService.findAllLessons();
        return ResponseEntity.ok(lessons);
    }

//    // Get lessons by course (ordered by orderIndex)
 @GetMapping("/course/{courseId}")
public ResponseEntity<List<Lesson>> getLessonsByCourse(@PathVariable Long courseId) {
List<Lesson> lessons = lessonService.findLessonsByCourse(courseId);
 return ResponseEntity.ok(lessons);
}
//
//    // Get lesson count for a course
    @GetMapping("/course/{courseId}/count")
    public ResponseEntity<Integer> getLessonCount(@PathVariable Long courseId) {
        int count = lessonService.countLessonsByCourse(courseId);
        return ResponseEntity.ok(count);
    }

    // Update lesson
    @PutMapping("/updateLesson/{id}")
    public ResponseEntity<Lesson> updateLesson(@PathVariable Long id, @RequestBody Lesson lesson) {
        lesson.setId(id);
        Lesson updated = lessonService.addOrUpdateLesson(lesson);
        return ResponseEntity.ok(updated);
    }

    // Delete lesson
    @DeleteMapping("/deleteLesson/{id}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Long id) {
        lessonService.deleteLesson(id);
        return ResponseEntity.noContent().build();
    }
}