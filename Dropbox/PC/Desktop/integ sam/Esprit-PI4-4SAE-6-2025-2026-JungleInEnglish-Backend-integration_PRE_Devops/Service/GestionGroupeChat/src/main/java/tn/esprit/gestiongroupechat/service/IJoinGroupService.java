package tn.esprit.gestiongroupechat.service;

import tn.esprit.gestiongroupechat.Dto.JoinGroupDTO;
import tn.esprit.gestiongroupechat.entities.JoinGroup;
import tn.esprit.gestiongroupechat.entities.Status;

import java.util.List;

public interface IJoinGroupService {

    JoinGroupDTO addRequest(JoinGroupDTO dto);

    Status getStatusByUserAndGroup(String userId, Long groupChatId);

    public List<JoinGroup>  getDemandeByidgroup (Long groupChatId );

    public void refuserDemande(Long joinGroupId);

    public void accepterDemande(Long joinGroupId);
}
