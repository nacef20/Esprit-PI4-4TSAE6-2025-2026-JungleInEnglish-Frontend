package tn.esprit.todomodule.controller;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import tn.esprit.todomodule.dto.TodoStatsDTO;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Todo;
import tn.esprit.todomodule.entity.TodoStatus;
import tn.esprit.todomodule.service.TodoService;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
public class TodoController {

    private final TodoService todoService;

    public TodoController(TodoService todoService) {
        this.todoService = todoService;
    }

    @GetMapping
    public ResponseEntity<List<Todo>> getAll() {
        return ResponseEntity.ok(todoService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Todo> getById(@PathVariable Long id) {
        return todoService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<List<Todo>> getByStatus(@PathVariable TodoStatus status) {
        return ResponseEntity.ok(todoService.findByStatus(status));
    }

    @GetMapping("/stats")
    public ResponseEntity<TodoStatsDTO> getStats() {
        return ResponseEntity.ok(todoService.getStats());
    }

    @PostMapping("/add")
    public ResponseEntity<Todo> create(
            @Valid @RequestBody Todo todo,
            JwtAuthenticationToken auth) {
        String userId = auth != null ? auth.getToken().getClaimAsString("sub") : "1";
        Todo created = todoService.create(todo, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PostMapping("/add-level")
    public ResponseEntity<String> addTodosToLevel(@RequestParam Level level, @RequestBody Todo todo) {
        todoService.addTodosToLevel(level, todo);
        return ResponseEntity.ok("Todos added to all students of level " + level);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Todo> update(
            @PathVariable Long id,
            @Valid @RequestBody Todo todo,
            JwtAuthenticationToken auth) {
        String userId = auth != null ? auth.getToken().getClaimAsString("sub") : "1";
        boolean isTeacher = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN"));
        Todo updated = todoService.update(id, todo, userId, isTeacher);
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Void> delete(@RequestParam Long id, JwtAuthenticationToken auth) {
        try {
            String userId = auth != null ? auth.getToken().getClaimAsString("sub") : "1";
            boolean isTeacher = auth != null && auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_TEACHER") || a.getAuthority().equals("ROLE_ADMIN"));
            todoService.deleteById(id, userId, isTeacher);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }
}
