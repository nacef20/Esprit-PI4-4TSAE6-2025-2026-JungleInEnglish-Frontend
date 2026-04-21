package tn.esprit.forumservice.mapper;


import org.mapstruct.Mapper;
import tn.esprit.forumservice.dto.ReactionDTO;
import tn.esprit.forumservice.entites.Reaction;

@Mapper(componentModel = "spring")
public interface ReactionMapper {

    ReactionDTO toDTO(Reaction entity);

    Reaction toEntity(ReactionDTO dto);

}
