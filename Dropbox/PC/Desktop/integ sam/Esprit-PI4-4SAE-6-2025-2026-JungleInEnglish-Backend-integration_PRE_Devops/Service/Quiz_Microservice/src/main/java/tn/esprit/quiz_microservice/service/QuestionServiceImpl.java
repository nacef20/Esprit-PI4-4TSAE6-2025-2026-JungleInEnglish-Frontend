package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Question;
import tn.esprit.quiz_microservice.repositories.QuestionRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class QuestionServiceImpl implements IQuestionService {

    private final QuestionRepository questionRepository;

    @Override
    public Question save(Question question) {
        return questionRepository.save(question);
    }

    @Override
    public List<Question> findAll() {
        return questionRepository.findAll();
    }

    @Override
    public List<Question> findByQuizId(Long quizId) {
        return questionRepository.findByQuizId(quizId);
    }

    @Override
    public Optional<Question> findById(Long id) {
        return questionRepository.findById(id);
    }

    @Override
    public Question update(Question question) {
        Question existing = questionRepository.findById(question.getId())
                .orElseThrow(() -> new RuntimeException("Question not found with id: " + question.getId()));
        existing.setContent(question.getContent());
        existing.setPoints(question.getPoints());
        existing.setQuiz(question.getQuiz());
        return questionRepository.save(existing);
    }

    @Override
    public void deleteById(Long id) {
        questionRepository.deleteById(id);
    }
}
