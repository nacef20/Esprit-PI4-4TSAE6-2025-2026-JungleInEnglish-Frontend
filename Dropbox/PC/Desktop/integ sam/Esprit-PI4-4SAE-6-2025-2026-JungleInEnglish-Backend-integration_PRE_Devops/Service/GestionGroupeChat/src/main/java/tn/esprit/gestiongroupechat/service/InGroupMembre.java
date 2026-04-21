package tn.esprit.gestiongroupechat.service;

import tn.esprit.gestiongroupechat.Dto.GroupMemberDTO;

import java.util.List;

public interface InGroupMembre {

    GroupMemberDTO addMember(GroupMemberDTO dto);

    List<GroupMemberDTO> getMembersByGroup(Long groupId);
    public void removeMember(Long groupId, String userId);
    public boolean isMember(Long groupId, String userId);
}
