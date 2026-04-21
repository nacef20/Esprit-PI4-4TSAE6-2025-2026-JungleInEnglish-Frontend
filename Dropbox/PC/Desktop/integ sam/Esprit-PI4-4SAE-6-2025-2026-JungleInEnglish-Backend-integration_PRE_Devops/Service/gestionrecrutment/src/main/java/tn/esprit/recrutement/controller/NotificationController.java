package tn.esprit.recrutement.controller;

import tn.esprit.recrutement.model.Notification;
import tn.esprit.recrutement.model.User;
import tn.esprit.recrutement.service.NotificationService;
import tn.esprit.recrutement.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;
    private final UserService userService;

    /**
     * GET /api/notifications?userId=1
     * Returns notifications for the given user (by role). Employer sees only their offers; Admin sees all.
     */
    @GetMapping
    public ResponseEntity<?> getNotifications(@RequestParam Integer userId) {
        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        List<Notification> notifications = notificationService.getNotificationsForUser(user);
        long unreadCount = notificationService.countUnreadForUser(user);
        return ResponseEntity.ok(Map.of(
                "notifications", notifications,
                "unreadCount", unreadCount
        ));
    }

    /**
     * PUT /api/notifications/{id}/read?userId=1
     * Marks the notification as read if it belongs to the user.
     */
    @PutMapping("/{id}/read")
    public ResponseEntity<?> markAsRead(@PathVariable Long id, @RequestParam Integer userId) {
        User user = userService.getUserById(userId).orElse(null);
        if (user == null) {
            return ResponseEntity.badRequest().body("User not found.");
        }
        Notification updated = notificationService.markAsRead(id, user);
        if (updated == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(updated);
    }
}
