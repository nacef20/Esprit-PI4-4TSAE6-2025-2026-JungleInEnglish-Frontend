package tn.esprit.todomodule.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "reminder")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Reminder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Reminder date and time is required")
    @Column(name = "reminder_date_time", nullable = false)
    private LocalDateTime reminderDateTime;

    @Column(length = 500)
    private String message;
    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "todo_id", nullable = false)
    private Todo todo;

    @Column(name = "is_triggered", nullable = false)
    private Boolean isTriggered = false;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public LocalDateTime getReminderDateTime() { return reminderDateTime; }
    public void setReminderDateTime(LocalDateTime reminderDateTime) { this.reminderDateTime = reminderDateTime; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Todo getTodo() { return todo; }
    public void setTodo(Todo todo) { this.todo = todo; }
    public Boolean getIsTriggered() { return isTriggered; }
    public void setIsTriggered(Boolean isTriggered) { this.isTriggered = isTriggered; }
}
