package tn.esprit.todomodule.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class CreateReminderRequest {

    @NotNull(message = "reminderDateTime is required")
    private LocalDateTime reminderDateTime;

    private String message;

    private Boolean isTriggered = false;

    public CreateReminderRequest() {
    }

    public CreateReminderRequest(LocalDateTime reminderDateTime, String message, Boolean isTriggered) {
        this.reminderDateTime = reminderDateTime;
        this.message = message;
        this.isTriggered = isTriggered != null ? isTriggered : false;
    }

    public LocalDateTime getReminderDateTime() { return reminderDateTime; }
    public void setReminderDateTime(LocalDateTime reminderDateTime) { this.reminderDateTime = reminderDateTime; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Boolean getIsTriggered() { return isTriggered; }
    public void setIsTriggered(Boolean isTriggered) { this.isTriggered = isTriggered; }
}
