package tn.esprit.forumservice.dto;

import tn.esprit.forumservice.entites.ReactionType;

import java.time.LocalDateTime;

public record ReactionDTO(
        Long idReaction,
        String userId,
        ReactionType type,
        LocalDateTime createdAt
) {
}
