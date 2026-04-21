package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.quiz_microservice.entities.Choice;

import java.util.List;

public interface ChoiceRepository extends JpaRepository<Choice, Long> {
    List<Choice> findByQuestionId(Long questionId);
}
