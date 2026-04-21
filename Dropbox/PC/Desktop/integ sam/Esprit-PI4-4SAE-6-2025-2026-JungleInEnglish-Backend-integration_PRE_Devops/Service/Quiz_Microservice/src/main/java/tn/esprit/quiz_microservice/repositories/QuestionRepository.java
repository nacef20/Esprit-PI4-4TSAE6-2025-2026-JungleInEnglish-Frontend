package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.quiz_microservice.entities.Question;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Long> {
    List<Question> findByQuizId(Long quizId);
}
