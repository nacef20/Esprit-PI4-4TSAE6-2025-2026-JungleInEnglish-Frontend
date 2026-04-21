package tn.esprit.gestiongroupechat.service;


import jakarta.validation.Valid;
import lombok.AllArgsConstructor;

import org.springframework.stereotype.Service;
import tn.esprit.gestiongroupechat.Dto.GroupChatDTO;
import tn.esprit.gestiongroupechat.Exception.GroupChatException;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.mapper.GroupChatMapper;
import tn.esprit.gestiongroupechat.repository.GroupChatRepo;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@AllArgsConstructor
public class GroupChatService implements InGroupChat {

 private final GroupChatRepo groupChatRepo;

 private final GroupChatMapper groupChatMapper;


    @Override
    public GroupChatDTO creategroup(GroupChatDTO request) {



        GroupChat entity = groupChatMapper.toEntity(request);

        entity.setImage(request.image());
        GroupChat saved = groupChatRepo.save(entity);

        return groupChatMapper.toDTO(saved);
    }

    @Override
    public List<GroupChatDTO> getAllGroups() {
        return groupChatRepo.findAll()
                .stream()
                .map(groupChatMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public GroupChatDTO getGroupById(Long id) {
        GroupChat group = groupChatRepo.findById(id)
                .orElseThrow(() -> new GroupChatException("Groupe non trouvé avec l'ID: " + id));
        return groupChatMapper.toDTO(group);
    }

    @Override
    public GroupChatDTO updateGroup(Long id, GroupChatDTO request) {
        GroupChat group = groupChatRepo.findById(id)
                .orElseThrow(() -> new GroupChatException("Groupe non trouvé avec l'ID: " + id));

        group.setName(request.name());
        group.setOwnerId(request.ownerId());
        group.setImage(request.image());

        GroupChat updated = groupChatRepo.save(group);
        return groupChatMapper.toDTO(updated);
    }

    @Override
    public void deleteGroup(Long id) {
        GroupChat group = groupChatRepo.findById(id)
                .orElseThrow(() -> new GroupChatException("Groupe non trouvé avec l'ID: " + id));
        groupChatRepo.delete(group);
    }


}
