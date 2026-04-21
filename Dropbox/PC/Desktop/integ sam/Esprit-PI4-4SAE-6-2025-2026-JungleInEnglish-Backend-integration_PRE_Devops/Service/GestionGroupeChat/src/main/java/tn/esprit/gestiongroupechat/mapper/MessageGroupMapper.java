package tn.esprit.gestiongroupechat.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import tn.esprit.gestiongroupechat.Dto.MessageGroupDTO;
import tn.esprit.gestiongroupechat.entities.MessageGroup;

@Mapper(componentModel = "spring")
public interface MessageGroupMapper {

    @Mapping(source = "groupChat.id", target = "groupChatId")
    @Mapping(source = "deleted", target = "deleted")
    MessageGroupDTO toDTO(MessageGroup entity);

    @Mapping(source = "groupChatId", target = "groupChat.id")
    @Mapping(source = "deleted", target = "deleted")
    MessageGroup toEntity(MessageGroupDTO dto);
}
