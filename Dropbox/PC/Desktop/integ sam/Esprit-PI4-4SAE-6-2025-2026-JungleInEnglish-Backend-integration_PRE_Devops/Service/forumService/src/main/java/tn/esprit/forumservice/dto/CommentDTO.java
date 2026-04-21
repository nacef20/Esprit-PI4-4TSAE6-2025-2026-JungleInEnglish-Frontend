package tn.esprit.forumservice.dto;

import java.time.LocalDateTime;

public record CommentDTO(Long id,
                         String content,
                         String authorid,
                         LocalDateTime dateSend) {
}
