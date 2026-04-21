package tn.esprit.todomodule.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.repository.TodoRepository;

import java.time.LocalDateTime;

@Component
public class CleanupScheduler {

    private static final Logger log = LoggerFactory.getLogger(CleanupScheduler.class);
    private final TodoRepository todoRepository;

    public CleanupScheduler(TodoRepository todoRepository) {
        this.todoRepository = todoRepository;
    }

    /**
     * Delete todos that have passed their due date.
     * Runs every hour at the top of the hour.
     */
    @Scheduled(cron = "0 0 * * * *")
    @Transactional
    public void cleanupExpiredTodos() {
        log.info("Starting cleanup of expired todos...");
        try {
            LocalDateTime now = LocalDateTime.now();
            todoRepository.deleteByDueDateBefore(now);
            log.info("Cleanup completed at {}", now);
        } catch (Exception e) {
            log.error("Cleanup failed: {}", e.getMessage());
        }
    }
}
