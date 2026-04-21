package tn.esprit.forumservice.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.forumservice.dto.PostDTO;
import tn.esprit.forumservice.entites.Post;

@Mapper(componentModel = "spring")
public interface PostMapper {

    @Mapping(source = "authorid", target = "authorid")

    PostDTO toDTO(Post entity);

    @Mapping(source = "authorid", target = "authorid")
    Post toEntity(PostDTO dto);
}
