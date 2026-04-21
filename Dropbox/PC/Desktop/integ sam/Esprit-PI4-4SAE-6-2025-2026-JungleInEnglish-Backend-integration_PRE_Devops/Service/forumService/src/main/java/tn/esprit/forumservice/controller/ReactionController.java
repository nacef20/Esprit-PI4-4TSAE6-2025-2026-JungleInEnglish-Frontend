package tn.esprit.forumservice.controller;


import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.forumservice.entites.Reaction;
import tn.esprit.forumservice.entites.ReactionType;
import tn.esprit.forumservice.service.ReactionService;

import java.util.List;

@RestController
@RequestMapping("/reactions")
@RequiredArgsConstructor
public class ReactionController {

    private final ReactionService reactionService;

    @PostMapping("/post/{postId}")
    public ResponseEntity<Reaction> react(
            @PathVariable Long postId,
            @RequestParam String userId,
            @RequestParam ReactionType type
    ) {
        return ResponseEntity.ok(reactionService.reactToPost(postId, userId, type));
    }

    @DeleteMapping("/post/{postId}")
    public ResponseEntity<Void> deleteReaction(
            @PathVariable Long postId,
            @RequestParam String userId
    ) {
        reactionService.deleteReaction(postId, userId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/post/{postId}")
    public ResponseEntity<List<Reaction>> getReactions(@PathVariable Long postId) {
        return ResponseEntity.ok(reactionService.getReactionsByPost(postId));
    }
}
