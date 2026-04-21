package tn.esprit.todomodule.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.entity.Reminder;
import tn.esprit.todomodule.entity.Todo;
import tn.esprit.todomodule.repository.ReminderRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ReminderScheduler {
    private final ReminderRepository reminderRepository;
    private final EmailService emailService;

    public ReminderScheduler(ReminderRepository reminderRepository, EmailService emailService) {
        this.reminderRepository = reminderRepository;
        this.emailService = emailService;
    }

    @Transactional
    @Scheduled(fixedRate = 60000) // every minute
    public void checkReminders() {
        LocalDateTime now = LocalDateTime.now();

        List<Reminder> dueReminders = reminderRepository
                .findByIsTriggeredFalseAndReminderDateTimeBefore(now);

        for (Reminder reminder : dueReminders) {
            Todo todo = reminder.getTodo();
            String recipientEmail = (todo.getNotificationEmail() != null && !todo.getNotificationEmail().isBlank()) 
                    ? todo.getNotificationEmail() 
                    : "brrn99amine@gmail.com";

            emailService.sendReminder(
                    recipientEmail,
                    "Deadline Passed: " + todo.getTitle(),
                    reminder.getMessage() != null ? reminder.getMessage() : "the todo has passed the deadline"
            );

            reminder.setIsTriggered(true);
            reminderRepository.save(reminder);
        }
    }
}

