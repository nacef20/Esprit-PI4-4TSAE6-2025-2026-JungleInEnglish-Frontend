package tn.esprit.gestiongroupechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestiongroupechat.entities.GroupChat;

@Repository
public interface GroupChatRepo extends JpaRepository<GroupChat, Long> {


}
