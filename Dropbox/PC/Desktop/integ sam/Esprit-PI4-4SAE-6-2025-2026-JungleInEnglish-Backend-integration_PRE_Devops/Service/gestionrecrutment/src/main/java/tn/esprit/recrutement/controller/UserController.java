package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.User;
import tn.esprit.recrutement.service.KeycloakUserClient;
import tn.esprit.recrutement.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class UserController {

    @Autowired
    private UserService userService;

    @Autowired
    private KeycloakUserClient keycloakUserClient;

    // -------------------------------------------------------------------------
    // User profile mapping endpoints
    // -------------------------------------------------------------------------

    @PostMapping("/add")
    public ResponseEntity<?> addUser(@RequestBody User user) {
        try {
            User created = userService.createUser(user);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("User creation failed: " + e.getMessage());
        }
    }

    @PutMapping("/update/{id}")
    public ResponseEntity<?> updateUser(@PathVariable int id, @RequestBody User user) {
        return ResponseEntity.ok(userService.updateUser(id, user));
    }

    // -------------------------------------------------------------------------
    // User listing — delegates to Keycloak via user-service
    // -------------------------------------------------------------------------

    @GetMapping("/all")
    public ResponseEntity<?> getAllUsers() {
        try {
            return ResponseEntity.ok(keycloakUserClient.getAllUsers());
        } catch (Exception e) {
            // Fallback: return local users if user-service is unreachable
            return ResponseEntity.ok(userService.getAllUsers());
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getUserById(@PathVariable int id) {
        Optional<User> localUser = userService.getUserById(id);
        if (localUser.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        User user = localUser.get();
        // If linked to Keycloak, fetch fresh data from user-service
        if (user.getKeycloakUserId() != null && !user.getKeycloakUserId().isBlank()) {
            try {
                return ResponseEntity.ok(keycloakUserClient.getUserById(user.getKeycloakUserId()));
            } catch (Exception ignored) {
                // Keycloak unreachable — return local data
            }
        }
        return ResponseEntity.ok(user);
    }


}