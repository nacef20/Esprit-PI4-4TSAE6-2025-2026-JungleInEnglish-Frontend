package tn.esprit.forumservice.mapper;

import org.mapstruct.Mapper;
import tn.esprit.forumservice.dto.CommentDTO;
import tn.esprit.forumservice.entites.Comment;

@Mapper(componentModel = "spring")
public interface CommentMapper {

    CommentDTO toDTO(Comment entity);

    Comment toEntity(CommentDTO dto);

}
