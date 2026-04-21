package com.fluently.lessonsservice.services;

import com.fluently.lessonsservice.client.CoursesServiceClient;
import com.fluently.lessonsservice.entities.Lesson;
import com.fluently.lessonsservice.repositories.LessonRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class LessonService implements ILessonService {

    @Autowired
    private LessonRepository lessonRepository;

    @Autowired
    private CoursesServiceClient coursesServiceClient;

    @Override
    public Lesson addOrUpdateLesson(Lesson lesson) {

        //avoid " Title " duplicates
        if (lesson.getTitle() != null) {
            lesson.setTitle(lesson.getTitle().trim());
        }

        //verify the course exists
        if (lesson.getId() == null) {
            if (!coursesServiceClient.courseExists(lesson.getCourseId())) {
                throw new RuntimeException("Course not found with id: " + lesson.getCourseId());
            }

            if (lessonRepository.existsByCourseIdAndOrderIndex(lesson.getCourseId(), lesson.getOrderIndex())) {
                throw new RuntimeException("Order index already used in this course.");
            }
            if (lessonRepository.existsByCourseIdAndTitleIgnoreCase(lesson.getCourseId(), lesson.getTitle())) {
                throw new RuntimeException("Lesson title already exists in this course.");
            }

            return lessonRepository.save(lesson);
        }

        // exclude current lesson id
        if (lessonRepository.existsByCourseIdAndOrderIndexAndIdNot(
                lesson.getCourseId(), lesson.getOrderIndex(), lesson.getId())) {
            throw new RuntimeException("Order index already used in this course.");
        }

        if (lessonRepository.existsByCourseIdAndTitleIgnoreCaseAndIdNot(
                lesson.getCourseId(), lesson.getTitle(), lesson.getId())) {
            throw new RuntimeException("Lesson title already exists in this course.");
        }

        return lessonRepository.save(lesson);
    }

    @Override
    public void deleteLesson(Long id) {
        lessonRepository.deleteById(id);
    }

    @Override
    public List<Lesson> findAllLessons() {
        return lessonRepository.findAll();
    }

    @Override
    public Lesson findLessonById(Long id) {
        return lessonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Lesson not found with id: " + id));
    }

    @Override
    public List<Lesson> findLessonsByCourse(Long courseId) {
        return lessonRepository.findByCourseIdOrderByOrderIndexAsc(courseId);
    }

    @Override
    public int countLessonsByCourse(Long courseId) {
        return lessonRepository.countByCourseId(courseId);
    }
}
