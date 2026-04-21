package tn.esprit.gestiongroupechat.Dto;

import jakarta.validation.constraints.NotNull;

import tn.esprit.gestiongroupechat.entities.Status;

import java.time.LocalDateTime;

public record JoinGroupDTO(

        Long idJoinGroup,
        Status status,
        LocalDateTime requestedAt,
        @NotNull(message = "Le groupChatId est obligatoire")
        Long groupChatId,
        String  userId
)  {}
