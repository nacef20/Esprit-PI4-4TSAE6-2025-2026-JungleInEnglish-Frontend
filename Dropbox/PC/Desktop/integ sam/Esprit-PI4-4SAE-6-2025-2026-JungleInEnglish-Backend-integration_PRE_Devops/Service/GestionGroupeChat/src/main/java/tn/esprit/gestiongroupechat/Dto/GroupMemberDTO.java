package tn.esprit.gestiongroupechat.Dto;

import jakarta.validation.constraints.NotNull;


import java.time.LocalDateTime;

public record GroupMemberDTO(Long idGroupMember,



                             @NotNull(message = "Le groupChatId est obligatoire")
                              Long groupChatId,
                             String userId,
                             LocalDateTime joinedAt) {
}
