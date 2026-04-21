package tn.esprit.todomodule.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.todomodule.entity.Reminder;

import java.time.LocalDateTime;
import java.util.List;

public interface ReminderRepository extends JpaRepository<Reminder, Long> {
    List<Reminder> findByIsTriggeredFalseAndReminderDateTimeBefore(LocalDateTime time);

    List<Reminder> findByTodoId(Long todoId);
}
