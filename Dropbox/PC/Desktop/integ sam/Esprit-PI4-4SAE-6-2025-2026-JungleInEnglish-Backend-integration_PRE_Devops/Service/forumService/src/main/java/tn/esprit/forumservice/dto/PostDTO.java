package tn.esprit.forumservice.dto;

import java.time.LocalDateTime;
import java.util.List;

public record PostDTO(
        Long idpost,
        String title,
        String content,
        String authorid,
        LocalDateTime createdAt,
        List<CommentDTO> comments,
        List<ReactionDTO> reactions
) {
}
