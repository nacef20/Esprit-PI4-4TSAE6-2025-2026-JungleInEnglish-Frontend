package tn.esprit.quiz_microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.entities.Question;
import tn.esprit.quiz_microservice.service.IQuestionService;

import java.util.List;

@RestController
@RequestMapping("/api/questions")

@RequiredArgsConstructor
public class QuestionController {

    private final IQuestionService questionService;

    @PostMapping
    public Question save(@Valid @RequestBody Question question) {
        return questionService.save(question);
    }

    @GetMapping
    public List<Question> findAll() {
        return questionService.findAll();
    }

    @GetMapping("/quiz/{quizId}")
    public List<Question> findByQuizId(@PathVariable Long quizId) {
        return questionService.findByQuizId(quizId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Question> findById(@PathVariable Long id) {
        return questionService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public Question update(@Valid @RequestBody Question question) {
        return questionService.update(question);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        questionService.deleteById(id);
    }
}
