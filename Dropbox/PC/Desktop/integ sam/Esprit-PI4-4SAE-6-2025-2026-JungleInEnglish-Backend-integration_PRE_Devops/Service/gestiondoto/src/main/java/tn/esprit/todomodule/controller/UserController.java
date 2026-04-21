package tn.esprit.todomodule.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.todomodule.entity.Level;
import tn.esprit.todomodule.entity.Role;
import tn.esprit.todomodule.entity.User;
import tn.esprit.todomodule.repository.UserRepository;
import tn.esprit.todomodule.service.KeycloakSyncService;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/todo-users")
public class UserController {

    @Autowired
    UserRepository userRepository;

    @Autowired
    KeycloakSyncService keycloakSyncService;

    @PostMapping("/sync-all")
    public ResponseEntity<String> syncFromKeycloak() {
        keycloakSyncService.syncAllUsers();
        return ResponseEntity.ok("Synchronization complete");
    }

    @GetMapping("/students")
    public List<User> getAllStudents() {
        return userRepository.findAllByRolesContains(Role.ROLE_STUDENT);
    }

    @PutMapping("/{id}/level")
    public ResponseEntity<?> updateUserLevel(@PathVariable String id, @RequestBody Map<String, String> payload) {
        return userRepository.findById(id)
                .map(user -> {
                    String levelStr = payload.get("level");
                    if (levelStr != null) {
                        try {
                            user.setLevel(Level.valueOf(levelStr.toUpperCase()));
                            userRepository.save(user);
                            return ResponseEntity.ok(user);
                        } catch (IllegalArgumentException e) {
                            return ResponseEntity.badRequest().body("Invalid level: " + levelStr);
                        }
                    }
                    return ResponseEntity.badRequest().body("Level is required");
                })
                .orElse(ResponseEntity.notFound().build());
    }
}
