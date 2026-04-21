package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.AttemptAnswer;
import tn.esprit.quiz_microservice.repositories.AttemptAnswerRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AttemptAnswerServiceImpl implements IAttemptAnswerService {

    private final AttemptAnswerRepository attemptAnswerRepository;

    @Override
    public List<AttemptAnswer> saveAll(List<AttemptAnswer> answers) {
        return attemptAnswerRepository.saveAll(answers);
    }

    @Override
    public List<AttemptAnswer> findByAttemptId(Long attemptId) {
        return attemptAnswerRepository.findByAttemptId(attemptId);
    }
}
