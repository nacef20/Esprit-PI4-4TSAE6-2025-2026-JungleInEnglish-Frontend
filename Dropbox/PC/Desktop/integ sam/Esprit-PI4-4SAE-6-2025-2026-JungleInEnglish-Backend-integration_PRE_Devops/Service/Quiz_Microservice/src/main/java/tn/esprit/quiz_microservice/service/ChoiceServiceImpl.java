package tn.esprit.quiz_microservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.quiz_microservice.entities.Choice;
import tn.esprit.quiz_microservice.repositories.ChoiceRepository;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ChoiceServiceImpl implements IChoiceService {

    private final ChoiceRepository choiceRepository;

    @Override
    public Choice save(Choice choice) {
        return choiceRepository.save(choice);
    }

    @Override
    public List<Choice> findAll() {
        return choiceRepository.findAll();
    }

    @Override
    public List<Choice> findByQuestionId(Long questionId) {
        return choiceRepository.findByQuestionId(questionId);
    }

    @Override
    public Optional<Choice> findById(Long id) {
        return choiceRepository.findById(id);
    }

    @Override
    public Choice update(Choice choice) {
        return choiceRepository.save(choice);
    }

    @Override
    public void deleteById(Long id) {
        choiceRepository.deleteById(id);
    }
}
