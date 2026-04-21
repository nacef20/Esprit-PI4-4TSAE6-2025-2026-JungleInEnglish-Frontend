package tn.esprit.todomodule.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.todomodule.dto.CreateReminderRequest;
import tn.esprit.todomodule.entity.Reminder;
import tn.esprit.todomodule.service.ReminderService;

import java.util.List;

@RestController
@RequestMapping("/api/reminders")
public class ReminderController {

    private final ReminderService reminderService;

    public ReminderController(ReminderService reminderService) {
        this.reminderService = reminderService;
    }

    @GetMapping

    public ResponseEntity<List<Reminder>> getAll() {
        return ResponseEntity.ok(reminderService.findAll());
    }

    @GetMapping("/{id}")

    public ResponseEntity<Reminder> getById(@PathVariable Long id) {
        return reminderService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/todo/{todoId}")

    public ResponseEntity<List<Reminder>> getByTodoId(@PathVariable Long todoId) {
        return ResponseEntity.ok(reminderService.findByTodoId(todoId));
    }

    @PostMapping("/add")

    public ResponseEntity<Reminder> create(
            @Valid @RequestBody CreateReminderRequest request,
            @RequestParam("todoId") Long todoId) {
        Reminder reminder = new Reminder();
        reminder.setReminderDateTime(request.getReminderDateTime());
        reminder.setMessage(request.getMessage());
        reminder.setIsTriggered(request.getIsTriggered() != null ? request.getIsTriggered() : false);
        Reminder created = reminderService.create(reminder, todoId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }


    @PutMapping("/update")

    public ResponseEntity<Reminder> update(@RequestParam Long id, @Valid @RequestBody Reminder reminder) {
        try {
            return ResponseEntity.ok(reminderService.update(id, reminder));
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/delete")

    public ResponseEntity<Void> delete(@RequestParam Long id) {
        try {
            reminderService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }
}
