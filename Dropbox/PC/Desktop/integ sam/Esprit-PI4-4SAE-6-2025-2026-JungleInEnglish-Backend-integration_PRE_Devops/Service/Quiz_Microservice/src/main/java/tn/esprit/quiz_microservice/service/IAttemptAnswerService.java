package tn.esprit.quiz_microservice.service;

import tn.esprit.quiz_microservice.entities.AttemptAnswer;

import java.util.List;

public interface IAttemptAnswerService {

    List<AttemptAnswer> saveAll(List<AttemptAnswer> answers);

    List<AttemptAnswer> findByAttemptId(Long attemptId);
}
