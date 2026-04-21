package tn.esprit.gestiongroupechat.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.gestiongroupechat.Dto.JoinGroupDTO;
import tn.esprit.gestiongroupechat.entities.JoinGroup;

@Mapper(componentModel = "spring")
public interface JoinGroupMapper {
    @Mapping(source = "groupChat.id", target = "groupChatId")
    JoinGroupDTO toDTO(JoinGroup entity);
    @Mapping(source = "groupChatId", target = "groupChat.id")
    JoinGroup toEntity(JoinGroupDTO dto);
}
