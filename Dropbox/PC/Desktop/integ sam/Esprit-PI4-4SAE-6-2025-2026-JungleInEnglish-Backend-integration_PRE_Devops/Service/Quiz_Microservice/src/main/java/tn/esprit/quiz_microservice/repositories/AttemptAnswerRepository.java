package tn.esprit.quiz_microservice.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import tn.esprit.quiz_microservice.entities.AttemptAnswer;

import java.util.List;

public interface AttemptAnswerRepository extends JpaRepository<AttemptAnswer, Long> {

    List<AttemptAnswer> findByAttemptId(Long attemptId);

    // Most failed questions: count total answers and wrong answers per question
    @Query("SELECT aa.question.id, aa.question.content, aa.question.quiz.title, COUNT(aa), " +
           "SUM(CASE WHEN aa.selectedChoice.isCorrect = false THEN 1 ELSE 0 END) " +
           "FROM AttemptAnswer aa WHERE aa.selectedChoice IS NOT NULL " +
           "GROUP BY aa.question.id, aa.question.content, aa.question.quiz.title " +
           "ORDER BY SUM(CASE WHEN aa.selectedChoice.isCorrect = false THEN 1 ELSE 0 END) DESC")
    List<Object[]> getMostFailedQuestions();
}
