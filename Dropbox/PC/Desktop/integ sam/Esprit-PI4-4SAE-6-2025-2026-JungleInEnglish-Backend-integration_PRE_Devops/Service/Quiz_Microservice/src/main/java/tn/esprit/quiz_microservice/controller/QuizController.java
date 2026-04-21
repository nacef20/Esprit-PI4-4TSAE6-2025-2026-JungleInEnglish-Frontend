package tn.esprit.quiz_microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.entities.Quiz;
import tn.esprit.quiz_microservice.service.IQuizService;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")

@RequiredArgsConstructor
public class QuizController {

    private final IQuizService quizService;

    @PostMapping
    public Quiz save(@Valid @RequestBody Quiz quiz) {
        return quizService.save(quiz);
    }

    @GetMapping
    public List<Quiz> findAll() {
        return quizService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Quiz> findById(@PathVariable Long id) {
        return quizService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public Quiz update(@Valid @RequestBody Quiz quiz) {
        return quizService.update(quiz);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        quizService.deleteById(id);
    }

    @GetMapping("/page")
    public Page<Quiz> findAllPaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String title) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        boolean hasStatus = status != null && !status.isEmpty();
        boolean hasTitle = title != null && !title.isEmpty();
        if (hasStatus && hasTitle) {
            return quizService.searchByTitleAndStatus(title, tn.esprit.quiz_microservice.entities.QuizStatus.valueOf(status), pageable);
        } else if (hasStatus) {
            return quizService.findByStatus(tn.esprit.quiz_microservice.entities.QuizStatus.valueOf(status), pageable);
        } else if (hasTitle) {
            return quizService.searchByTitle(title, pageable);
        }
        return quizService.findAllPaginated(pageable);
    }

    @GetMapping("/page/available")
    public Page<Quiz> findAvailablePaginated(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "6") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String filter,
            @RequestParam(required = false) Long studentId) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        boolean hasTitle = title != null && !title.isEmpty();
        boolean hasFilter = filter != null && !filter.isEmpty() && !"all".equals(filter);

        if (hasFilter && studentId != null) {
            switch (filter) {
                case "open":
                    return hasTitle
                            ? quizService.searchOpenForStudentByTitle(title, studentId, pageable)
                            : quizService.findOpenForStudent(studentId, pageable);
                case "attempted":
                    return hasTitle
                            ? quizService.searchAttemptedByStudentByTitle(title, studentId, pageable)
                            : quizService.findAttemptedByStudent(studentId, pageable);
                case "closed":
                    return hasTitle
                            ? quizService.searchByTitleAndStatus(title, tn.esprit.quiz_microservice.entities.QuizStatus.CLOSED, pageable)
                            : quizService.findByStatus(tn.esprit.quiz_microservice.entities.QuizStatus.CLOSED, pageable);
            }
        }
        return hasTitle
                ? quizService.searchAvailableByTitle(title, pageable)
                : quizService.findAvailablePaginated(pageable);
    }
}
