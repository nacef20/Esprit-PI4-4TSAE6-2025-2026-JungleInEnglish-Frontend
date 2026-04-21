package tn.esprit.gestiongroupechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tn.esprit.gestiongroupechat.entities.GroupChat;
import tn.esprit.gestiongroupechat.entities.MessageGroup;

import java.util.Arrays;
import java.util.List;

public interface MessageRepo extends JpaRepository<MessageGroup, Long> {

    List<MessageGroup> findByGroupChat(GroupChat groupChat);

}
