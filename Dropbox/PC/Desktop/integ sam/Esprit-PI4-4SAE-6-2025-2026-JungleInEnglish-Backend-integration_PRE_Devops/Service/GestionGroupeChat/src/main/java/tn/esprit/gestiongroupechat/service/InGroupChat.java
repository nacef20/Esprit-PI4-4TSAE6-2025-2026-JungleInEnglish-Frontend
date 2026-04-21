package tn.esprit.gestiongroupechat.service;


import tn.esprit.gestiongroupechat.Dto.GroupChatDTO;

import java.util.List;

public interface InGroupChat {
    GroupChatDTO creategroup(GroupChatDTO request);
    List<GroupChatDTO> getAllGroups();
    GroupChatDTO getGroupById(Long id);
    GroupChatDTO updateGroup(Long id, GroupChatDTO request);
    void deleteGroup(Long id);
}
