package tn.esprit.forumservice.controller;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forumservice.dto.CommentDTO;
import tn.esprit.forumservice.service.CommentService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/comments")
@RequiredArgsConstructor

public class CommentController {
    private final CommentService commentService;


    // 🔹 CREATE COMMENT
    @PostMapping("/post/{postId}")
    public ResponseEntity<?> createComment(
            @PathVariable Long postId,
            @RequestBody @Valid CommentDTO request) {

        try {
            CommentDTO created = commentService.createComment(postId, request);
            return ResponseEntity.ok(created);
        } catch (RuntimeException e) {
            // Renvoie un JSON avec le message d'erreur
            Map<String, String> error = new HashMap<>();
            error.put("error", e.getMessage());
            return ResponseEntity.badRequest().body(error);
        }
    }

    // 🔹 GET COMMENTS BY POST
    @GetMapping("/post/{postId}")
    public ResponseEntity<List<CommentDTO>> getCommentsByPost(@PathVariable Long postId) {
        return ResponseEntity.ok(commentService.getCommentsByPost(postId));
    }

    // 🔹 DELETE COMMENT
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok().build();
    }

}
