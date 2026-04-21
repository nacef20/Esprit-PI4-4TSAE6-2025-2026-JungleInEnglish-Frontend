package tn.esprit.gestiongroupechat.mapper;


import org.mapstruct.Mapper;
import tn.esprit.gestiongroupechat.Dto.GroupChatDTO;
import tn.esprit.gestiongroupechat.entities.GroupChat;

@Mapper(componentModel = "spring")
public interface GroupChatMapper  {

    GroupChatDTO toDTO(GroupChat entity);

    GroupChat toEntity(GroupChatDTO dto);



}
