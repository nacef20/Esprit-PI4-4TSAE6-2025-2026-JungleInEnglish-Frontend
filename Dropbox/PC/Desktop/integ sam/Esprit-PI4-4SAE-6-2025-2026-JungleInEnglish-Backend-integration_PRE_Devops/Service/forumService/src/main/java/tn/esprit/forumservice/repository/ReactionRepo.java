package tn.esprit.forumservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forumservice.entites.Reaction;

import java.util.List;
import java.util.Optional;

@Repository
public interface ReactionRepo extends JpaRepository<Reaction, Long> {

    Optional<Reaction> findByPostIdpostAndUserId(Long postId, String userId);
    List<Reaction> findByPostIdpost(Long postId);
}
