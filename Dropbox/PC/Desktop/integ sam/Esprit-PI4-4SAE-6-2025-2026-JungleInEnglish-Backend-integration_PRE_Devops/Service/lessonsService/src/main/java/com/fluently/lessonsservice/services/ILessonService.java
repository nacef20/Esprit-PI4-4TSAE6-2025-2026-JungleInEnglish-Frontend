package com.fluently.lessonsservice.services;

import com.fluently.lessonsservice.entities.Lesson;
import java.util.List;

public interface ILessonService {
    Lesson addOrUpdateLesson(Lesson lesson);
    void deleteLesson(Long id);
    List<Lesson> findAllLessons();
    Lesson findLessonById(Long id);
   List<Lesson> findLessonsByCourse(Long courseId);
    int countLessonsByCourse(Long courseId);
}