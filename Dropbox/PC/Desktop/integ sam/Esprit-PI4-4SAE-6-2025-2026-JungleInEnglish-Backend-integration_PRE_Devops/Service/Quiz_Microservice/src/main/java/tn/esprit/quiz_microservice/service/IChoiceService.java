package tn.esprit.quiz_microservice.service;

import tn.esprit.quiz_microservice.entities.Choice;

import java.util.List;
import java.util.Optional;

public interface IChoiceService {

    Choice save(Choice choice);

    List<Choice> findAll();

    List<Choice> findByQuestionId(Long questionId);

    Optional<Choice> findById(Long id);

    Choice update(Choice choice);

    void deleteById(Long id);
}
