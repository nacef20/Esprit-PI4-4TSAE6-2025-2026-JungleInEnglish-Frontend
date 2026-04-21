package tn.esprit.todomodule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.entity.Reminder;
import tn.esprit.todomodule.entity.Todo;
import tn.esprit.todomodule.repository.ReminderRepository;
import tn.esprit.todomodule.repository.TodoRepository;

import java.util.List;
import java.util.Optional;

@Service
public class ReminderService {

    private final ReminderRepository reminderRepository;
    private final TodoRepository todoRepository;

    public ReminderService(ReminderRepository reminderRepository, TodoRepository todoRepository) {
        this.reminderRepository = reminderRepository;
        this.todoRepository = todoRepository;
    }

    @Transactional(readOnly = true)
    public List<Reminder> findAll() {
        return reminderRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Reminder> findById(Long id) {
        return reminderRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Reminder> findByTodoId(Long todoId) {
        return reminderRepository.findByTodoId(todoId);
    }

    @Transactional
    public Reminder create(Reminder reminder, Long todoId) {
        if (reminder.getReminderDateTime() == null) {
            throw new IllegalArgumentException("reminderDateTime is required");
        }
        Todo todo = todoRepository.findById(todoId)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + todoId));
        reminder.setTodo(todo);
        return reminderRepository.save(reminder);
    }


    @Transactional
    public Reminder update(Long id, Reminder reminder) {
        return reminderRepository.findById(id)
                .map(existing -> {
                    existing.setReminderDateTime(reminder.getReminderDateTime());
                    existing.setMessage(reminder.getMessage());
                    existing.setIsTriggered(reminder.getIsTriggered());
                    if (reminder.getTodo() != null && reminder.getTodo().getId() != null) {
                        Todo todo = todoRepository.findById(reminder.getTodo().getId())
                                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + reminder.getTodo().getId()));
                        existing.setTodo(todo);
                    }
                    return reminderRepository.save(existing);
                })
                .orElseThrow(() -> new RuntimeException("Reminder not found with id: " + id));
    }

    @Transactional
    public void deleteById(Long id) {
        if (!reminderRepository.existsById(id)) {
            throw new RuntimeException("Reminder not found with id: " + id);
        }
        reminderRepository.deleteById(id);
    }
}
