package tn.esprit.quiz_microservice.service;

import tn.esprit.quiz_microservice.entities.Question;

import java.util.List;
import java.util.Optional;

public interface IQuestionService {

    Question save(Question question);

    List<Question> findAll();

    List<Question> findByQuizId(Long quizId);

    Optional<Question> findById(Long id);

    Question update(Question question);

    void deleteById(Long id);
}
