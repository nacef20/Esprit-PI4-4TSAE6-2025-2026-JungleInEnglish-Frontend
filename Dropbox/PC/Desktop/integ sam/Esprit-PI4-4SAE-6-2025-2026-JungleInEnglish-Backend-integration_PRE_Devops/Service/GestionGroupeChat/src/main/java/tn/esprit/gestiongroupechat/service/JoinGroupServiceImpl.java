package tn.esprit.gestiongroupechat.service;

import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import tn.esprit.gestiongroupechat.Dto.JoinGroupDTO;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.entities.GroupMember;
import tn.esprit.gestiongroupechat.entities.JoinGroup;
import tn.esprit.gestiongroupechat.entities.Status;
import tn.esprit.gestiongroupechat.mapper.GroupChatMapper;
import tn.esprit.gestiongroupechat.mapper.JoinGroupMapper;
import tn.esprit.gestiongroupechat.repository.GroupChatRepo;
import tn.esprit.gestiongroupechat.repository.JoinGroupRepo;
import tn.esprit.gestiongroupechat.repository.MembreGroupRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JoinGroupServiceImpl implements IJoinGroupService {

    private final JoinGroupRepo joinGroupRepo;
    private final GroupChatRepo groupChatRepo;
    private final JoinGroupMapper joinGroupMapper;
    private final MembreGroupRepo membreGroupRepo;


    @Override
    public JoinGroupDTO addRequest(JoinGroupDTO dto) {
        // Récupérer le group chat
        GroupChat groupChat = groupChatRepo.findById(dto.groupChatId())
                .orElseThrow(() -> new RuntimeException("GroupChat not found"));


        JoinGroup entity = joinGroupMapper.toEntity(dto);
        entity.setGroupChat(groupChat);


        entity.setStatus(Status.ATTENTE);

        // Enregistrer
        JoinGroup saved = joinGroupRepo.save(entity);

        return joinGroupMapper.toDTO(saved);
    }


    @Override
    public Status getStatusByUserAndGroup(String  userId, Long groupChatId) {
        List<JoinGroup> joinGroups = joinGroupRepo.findByUserIdAndGroupChatId(userId, groupChatId);
        if (joinGroups.isEmpty()) {
            return null;
        }
        return joinGroups.get(0).getStatus();
    }

    @Override
    public List<JoinGroup> getDemandeByidgroup(Long groupChatId) {
        return joinGroupRepo.findByGroupChatId(groupChatId);
    }

    @Override
    public void accepterDemande(Long joinGroupId) {
        JoinGroup demande = joinGroupRepo.findById(joinGroupId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        demande.setStatus(Status.ACCEPTE);

        GroupMember membre = new GroupMember();
        membre.setUserId(demande.getUserId());
        membre.setGroupChat(demande.getGroupChat());
       membreGroupRepo.save(membre);

        joinGroupRepo.save(demande);
    }

    @Override
    public void refuserDemande(Long joinGroupId) {
        JoinGroup demande = joinGroupRepo.findById(joinGroupId)
                .orElseThrow(() -> new RuntimeException("Demande introuvable"));
        demande.setStatus(Status.REFUSE);
        joinGroupRepo.save(demande);
    }

    @Scheduled(cron = "0 0 0 * * *")
    public void cleanExpiredJoinRequests() {
        LocalDateTime now = LocalDateTime.now();

        List<JoinGroup> joinGroups = joinGroupRepo.findByStatusIn(
                List.of(Status.ATTENTE, Status.REFUSE)
        );

        for (JoinGroup join : joinGroups) {
            if (join.getRequestedAt().plusDays(7).isBefore(now)) {
                // Supprimer ou logger avant suppression
                System.out.println("Suppression JoinGroup ID: " + join.getIdJoinGroup());
                joinGroupRepo.delete(join);
            }
        }
    }

    // Exécute le job chaque minute
    @Scheduled(cron = "0 * * * * *")
    public void cleanExpiredJoinRequestss() {
        System.out.println("supprimer1");
        LocalDateTime now = LocalDateTime.now();

        List<JoinGroup> joinGroups = joinGroupRepo.findByStatusIn(
                List.of(Status.ATTENTE, Status.REFUSE ,Status.ACCEPTE)
        );

        for (JoinGroup join : joinGroups) {
            // Ici on simule "7 jours = 1 minute"
            if (join.getRequestedAt().plusMinutes(1).isBefore(now)) {
                System.out.println("Suppression JoinGroup ID: " + join.getIdJoinGroup());
                joinGroupRepo.delete(join);
            }
        }
    }



}
