package tn.esprit.gestiongroupechat.mapper;

import org.mapstruct.Mapper;

import tn.esprit.gestiongroupechat.Dto.GroupMemberDTO;
import tn.esprit.gestiongroupechat.Dto.JoinGroupDTO;

import tn.esprit.gestiongroupechat.entities.GroupMember;
import tn.esprit.gestiongroupechat.entities.JoinGroup;

@Mapper(componentModel = "spring")
public interface GroupMemberMapper {

    GroupMemberDTO toDTO(GroupMember entity);

    GroupMember toEntity(GroupMemberDTO dto);
}
