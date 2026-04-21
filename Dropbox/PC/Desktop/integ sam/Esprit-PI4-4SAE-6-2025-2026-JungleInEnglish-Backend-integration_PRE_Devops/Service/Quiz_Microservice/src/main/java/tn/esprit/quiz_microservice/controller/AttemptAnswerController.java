package tn.esprit.quiz_microservice.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import tn.esprit.quiz_microservice.entities.AttemptAnswer;
import tn.esprit.quiz_microservice.service.IAttemptAnswerService;

import java.util.List;

@RestController
@RequestMapping("/api/attempt-answers")

@RequiredArgsConstructor
public class AttemptAnswerController {

    private final IAttemptAnswerService attemptAnswerService;

    @PostMapping
    public List<AttemptAnswer> saveAll(@RequestBody List<AttemptAnswer> answers) {
        return attemptAnswerService.saveAll(answers);
    }

    @GetMapping("/attempt/{attemptId}")
    public List<AttemptAnswer> findByAttemptId(@PathVariable Long attemptId) {
        return attemptAnswerService.findByAttemptId(attemptId);
    }
}
