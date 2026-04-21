package tn.esprit.todomodule.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tn.esprit.todomodule.dto.TodoStatsDTO;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Priority;
import tn.esprit.todomodule.entity.Reminder;
import tn.esprit.todomodule.entity.Todo;
import tn.esprit.todomodule.entity.TodoStatus;
import tn.esprit.todomodule.repository.ReminderRepository;
import tn.esprit.todomodule.repository.TodoRepository;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class TodoService {

    private final TodoRepository todoRepository;
    private final EmailService emailService;
    private final ReminderRepository reminderRepository;
    private final tn.esprit.todomodule.repository.UserRepository userRepository;

    public TodoService(TodoRepository todoRepository, EmailService emailService, ReminderRepository reminderRepository, tn.esprit.todomodule.repository.UserRepository userRepository) {
        this.todoRepository = todoRepository;
        this.emailService = emailService;
        this.reminderRepository = reminderRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<Todo> findAll() {
        return todoRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Todo> findForTeacher(String userId) {
        return todoRepository.findByLevelIsNotNullOrUserId(userId);
    }

    @Transactional(readOnly = true)
    public Optional<Todo> findById(Long id) {
        return todoRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public List<Todo> findByStatus(TodoStatus status) {
        return todoRepository.findByStatus(status);
    }

    @Transactional(readOnly = true)
    public List<Todo> findByLevelOrUser(Level level, String userId) {
        if (level == null) {
            return todoRepository.findByUser(userRepository.getReferenceById(userId));
        }
        return todoRepository.findByLevelOrUserId(level, userId);
    }

    @Transactional(readOnly = true)
    public TodoStatsDTO getStats() {
        List<Todo> todos = todoRepository.findAll();
        long total = todos.size();
        if (total == 0) {
            return new TodoStatsDTO(0, 0, 0, 0, 0, new HashMap<>(), new HashMap<>());
        }

        // 1. Completion Metrics (Checked box counts as completed in UI logic)
        long completedCount = todos.stream()
                .filter(t -> Boolean.TRUE.equals(t.getChecked()) || t.getStatus() == TodoStatus.COMPLETED)
                .count();

        // 2. Pending Metrics (Not checked and Status is PENDING)
        long pendingCount = todos.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getChecked()) && t.getStatus() == TodoStatus.PENDING)
                .count();

        // 3. Priority Metrics
        long highPriorityCount = todos.stream()
                .filter(t -> t.getPriority() == Priority.HIGH)
                .count();
        
        // 4. Overdue Metrics
        LocalDateTime now = LocalDateTime.now();
        long overdueCount = todos.stream()
                .filter(t -> !Boolean.TRUE.equals(t.getChecked()) && 
                           t.getDueDate() != null && 
                           t.getDueDate().isBefore(now))
                .count();

        // 5. Chart Data
        Map<String, Long> statusBreakdown = todos.stream()
                .collect(Collectors.groupingBy(t -> t.getStatus().name(), Collectors.counting()));
        
        Map<String, Long> priorityDistribution = todos.stream()
                .collect(Collectors.groupingBy(t -> t.getPriority().name(), Collectors.counting()));

        double completedPercentage = (double) completedCount / total * 100;
        double overduePercentage = (double) overdueCount / total * 100;

        return new TodoStatsDTO(
                total,
                completedPercentage,
                overduePercentage,
                highPriorityCount,
                pendingCount,
                statusBreakdown,
                priorityDistribution
        );
    }

    @Transactional
    public Todo create(Todo todo, String userId) {
        tn.esprit.todomodule.entity.User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found after sync. Please try again."));
        todo.setUser(user);
        Todo savedTodo = todoRepository.save(todo);
        createOrUpdateReminder(savedTodo);
        
        // Wrap notifications in try-catch to avoid transaction rollback if email service fails
        try {
            sendNotifications(savedTodo, "New Todo Created: " + savedTodo.getTitle());
        } catch (Exception e) {
            System.err.println("Failed to send notification: " + e.getMessage());
        }
        
        return savedTodo;
    }

    @Transactional
    public void addTodosToLevel(Level level, Todo todoTemplate) {
        List<tn.esprit.todomodule.entity.User> students = userRepository.findAllByLevel(level);
        for (tn.esprit.todomodule.entity.User student : students) {
            Todo newTodo = new Todo();
            newTodo.setTitle(todoTemplate.getTitle());
            newTodo.setDescription(todoTemplate.getDescription());
            newTodo.setPriority(todoTemplate.getPriority());
            newTodo.setStatus(TodoStatus.PENDING);
            newTodo.setDueDate(todoTemplate.getDueDate());
            newTodo.setLevel(level);
            newTodo.setUser(student);
            newTodo.setChecked(false);
            newTodo.setNotificationEmail(student.getEmail());
            
            Todo saved = todoRepository.save(newTodo);
            createOrUpdateReminder(saved);
        }
    }

    private void createOrUpdateReminder(Todo todo) {
        if (todo.getDueDate() != null) {
            Reminder reminder = reminderRepository.findByTodoId(todo.getId())
                    .stream().findFirst().orElse(new Reminder());
            
            reminder.setTodo(todo);
            reminder.setReminderDateTime(todo.getDueDate());
            reminder.setIsTriggered(false);
            reminder.setMessage("the todo has passed the deadline");
            reminderRepository.save(reminder);
        }
    }

    private void sendNotifications(Todo todo, String message) {
        if (todo.getNotificationEmail() != null && !todo.getNotificationEmail().isBlank()) {
            emailService.sendReminder(todo.getNotificationEmail(), "Todo Notification", message);
        }
    }

    @Transactional
    public Todo update(Long id, Todo todo, String userId, boolean isTeacher) {
        return todoRepository.findById(id)
                .map(existing -> {
                    // --- Authorization Checks ---

                    // 1. If it's a Level Mission (has a level), only Teachers/Admins can toggle completion
                    if (existing.getLevel() != null && !isTeacher) {
                        // Check if the student is trying to change 'checked' or 'status'
                        if (todo.getChecked() != null || todo.getStatus() != null) {
                            throw new RuntimeException("Only teachers can mark level missions as complete.");
                        }
                    }

                    // 2. If it's a private todo (no level), student must be the owner
                    if (existing.getLevel() == null && !isTeacher) {
                        if (existing.getUser() == null || !existing.getUser().getId().equals(userId)) {
                            throw new RuntimeException("You are not authorized to edit this todo.");
                        }
                    }

                    // --- Apply Updates ---
                    if (todo.getTitle() != null) {
                        if (todo.getTitle().isBlank()) {
                            throw new IllegalArgumentException("Title cannot be blank");
                        }
                        existing.setTitle(todo.getTitle());
                    }

                    if (todo.getDescription() != null) {
                        existing.setDescription(todo.getDescription());
                    }

                    if (todo.getStatus() != null) {
                        existing.setStatus(todo.getStatus());
                    }

                    if (todo.getPriority() != null) {
                        existing.setPriority(todo.getPriority());
                    }

                    if (todo.getDueDate() != null) {
                        existing.setDueDate(todo.getDueDate());
                    }

                    if (todo.getLevel() != null) {
                        existing.setLevel(todo.getLevel());
                    }


                    if (todo.getChecked() != null) {
                        existing.setChecked(todo.getChecked());
                        if (Boolean.TRUE.equals(todo.getChecked())) {
                            existing.setStatus(TodoStatus.COMPLETED);
                        } else if (existing.getStatus() == TodoStatus.COMPLETED) {
                            existing.setStatus(TodoStatus.PENDING);
                        }
                    }



                    if (todo.getNotificationEmail() != null) {
                        existing.setNotificationEmail(todo.getNotificationEmail());
                    }

                    Todo updatedTodo = todoRepository.save(existing);
                    createOrUpdateReminder(updatedTodo);
                    sendNotifications(updatedTodo, "Todo Updated: " + updatedTodo.getTitle());
                    return updatedTodo;

                })
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));
    }

    @Transactional
    public void deleteById(Long id, String userId, boolean isTeacher) {
        Todo todo = todoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Todo not found with id: " + id));

        // If student, check if they own the todo
        if (!isTeacher) {
            if (todo.getUser() == null || !todo.getUser().getId().equals(userId)) {
                throw new RuntimeException("You are not authorized to delete this todo");
            }
        }

        todoRepository.delete(todo);
    }
}
