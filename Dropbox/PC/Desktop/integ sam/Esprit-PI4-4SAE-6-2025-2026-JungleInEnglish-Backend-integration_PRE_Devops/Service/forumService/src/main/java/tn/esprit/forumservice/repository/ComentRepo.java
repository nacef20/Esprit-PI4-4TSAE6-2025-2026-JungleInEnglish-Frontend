package tn.esprit.forumservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import tn.esprit.forumservice.entites.Comment;
import tn.esprit.forumservice.entites.Post;

import java.util.List;

@Repository
public interface ComentRepo extends JpaRepository<Comment, Long> {

    List<Comment> findByPostIdpost(Long postId);

    Comment findTopByPostAndAuthoridOrderByDateSendDesc(Post post, String authorid);
}
