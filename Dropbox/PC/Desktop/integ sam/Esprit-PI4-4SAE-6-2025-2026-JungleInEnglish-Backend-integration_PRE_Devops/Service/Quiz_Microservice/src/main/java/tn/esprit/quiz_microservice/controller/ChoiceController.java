package tn.esprit.quiz_microservice.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.entities.Choice;
import tn.esprit.quiz_microservice.service.IChoiceService;

import java.util.List;

@RestController
@RequestMapping("/api/choices")

@RequiredArgsConstructor
public class ChoiceController {

    private final IChoiceService choiceService;

    @PostMapping
    public Choice save(@Valid @RequestBody Choice choice) {
        return choiceService.save(choice);
    }

    @GetMapping
    public List<Choice> findAll() {
        return choiceService.findAll();
    }

    @GetMapping("/question/{questionId}")
    public List<Choice> findByQuestionId(@PathVariable Long questionId) {
        return choiceService.findByQuestionId(questionId);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Choice> findById(@PathVariable Long id) {
        return choiceService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping
    public Choice update(@Valid @RequestBody Choice choice) {
        return choiceService.update(choice);
    }

    @DeleteMapping("/{id}")
    public void deleteById(@PathVariable Long id) {
        choiceService.deleteById(id);
    }
}
