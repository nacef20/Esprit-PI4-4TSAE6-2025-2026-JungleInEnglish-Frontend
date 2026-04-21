package tn.esprit.gestiongroupechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestiongroupechat.entities.GroupMember;

import java.util.List;

@Repository
public interface MembreGroupRepo extends JpaRepository <GroupMember ,Long> {


    List<GroupMember> findByGroupChatId(Long groupChatId);




    GroupMember findByGroupChatIdAndUserId(Long groupId, String userId);

}
