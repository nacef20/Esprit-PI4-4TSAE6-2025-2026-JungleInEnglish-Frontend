package tn.esprit.gestiongroupechat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import tn.esprit.gestiongroupechat.Dto.GroupMemberDTO;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.entities.GroupMember;
import tn.esprit.gestiongroupechat.mapper.GroupMemberMapper;
import tn.esprit.gestiongroupechat.repository.GroupChatRepo;
import tn.esprit.gestiongroupechat.repository.MembreGroupRepo;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class  GroupeMenbreService implements InGroupMembre {

    private final MembreGroupRepo groupMemberRepository;
    private final GroupChatRepo groupChatRepository;
    private final GroupMemberMapper groupMemberMapper;
    private final MembreGroupRepo membreGroupRepo;

    @Override
    public GroupMemberDTO addMember(GroupMemberDTO dto) {
        GroupChat groupChat = groupChatRepository.findById(dto.groupChatId())
                .orElseThrow(() -> new RuntimeException("GroupChat not found"));

        GroupMember entity = groupMemberMapper.toEntity(dto);
        entity.setGroupChat(groupChat);

        GroupMember saved = groupMemberRepository.save(entity);

        return groupMemberMapper.toDTO(saved);
    }

    @Override
    public List<GroupMemberDTO> getMembersByGroup(Long groupId) {
        return groupMemberRepository.findByGroupChatId(groupId)
                .stream()
                .map(groupMemberMapper::toDTO)
                .toList();
    }

    @Override
    public void removeMember(Long groupId, String userId) {

        GroupChat group = groupChatRepository.findById(groupId)
                .orElseThrow(() -> new RuntimeException("Group not found"));

        if (group.getOwnerId().equals(userId)) {
            throw new RuntimeException("Cannot remove group admin");
        }

        GroupMember member = groupMemberRepository.findByGroupChatIdAndUserId(groupId, userId);

        if (member == null) {
            throw new RuntimeException("Member not found");
        }

        groupMemberRepository.delete(member);
    }


    @Override
    public boolean isMember(Long groupId, String userId) {
        if(membreGroupRepo.findByGroupChatIdAndUserId(groupId, userId)!=null){
            return true;
        }
        return false;
    }

}
