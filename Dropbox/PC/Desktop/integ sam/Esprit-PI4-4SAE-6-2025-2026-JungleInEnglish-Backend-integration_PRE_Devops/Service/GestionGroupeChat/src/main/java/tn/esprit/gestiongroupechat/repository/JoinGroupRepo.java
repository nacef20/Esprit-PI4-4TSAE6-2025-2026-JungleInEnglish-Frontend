package tn.esprit.gestiongroupechat.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.gestiongroupechat.entities.JoinGroup;
import tn.esprit.gestiongroupechat.entities.Status;

import java.util.List;
import java.util.Optional;

@Repository
public interface JoinGroupRepo extends JpaRepository<JoinGroup, Long> {


    List<JoinGroup> findByUserIdAndGroupChatId(String userId, Long groupChatId);

    List<JoinGroup> findByGroupChatId(Long groupChatId);

    List<JoinGroup> findByStatusIn(List<Status> statuses);
}
