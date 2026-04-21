package tn.esprit.gestiongroupechat.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;



public record MessageGroupDTO(Long idMessageChat,
                              @NotNull(message = "Le sender est obligatoire")
                              String  sender,
                              @NotBlank(message = "Le contenu est obligatoire")
                               String content,
                              Long groupChatId,
                              LocalDateTime dateSend,
                              boolean deleted) {
}
