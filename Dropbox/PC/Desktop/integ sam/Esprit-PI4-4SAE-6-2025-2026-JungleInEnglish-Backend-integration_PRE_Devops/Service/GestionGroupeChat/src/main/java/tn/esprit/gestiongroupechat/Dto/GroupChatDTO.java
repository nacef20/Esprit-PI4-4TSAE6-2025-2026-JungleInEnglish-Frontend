package tn.esprit.gestiongroupechat.Dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Date;

public record GroupChatDTO( Long id,
                            @NotBlank(message = "Le nom du groupe est obligatoire")
                            String name,
                            @NotNull(message = "Le propriétaire est obligatoire")
                           String ownerId,
                            String image) {

}
