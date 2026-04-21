package tn.esprit.forumservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.forumservice.dto.CommentDTO;
import tn.esprit.forumservice.entites.Comment;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.mapper.CommentMapper;
import tn.esprit.forumservice.repository.ComentRepo;
import tn.esprit.forumservice.repository.PostRepo;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService implements ComentIn{

    private final ComentRepo commentRepository;
    private final PostRepo postRepository;
    private final CommentMapper commentMapper;
    private final SimpMessagingTemplate messagingTemplate;


    @Override
    public CommentDTO createComment(Long postId, CommentDTO dto) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // ✅ Vérifications avant création
        if (PostValidator.containsSpam(dto.content())) {
            throw new RuntimeException("Commentaire considéré comme spam !");
        }

        if (PostValidator.containsBlockedLink(dto.content())) {
            throw new RuntimeException("Lien interdit !");
        }

        // Vérifier flood : on prend le dernier commentaire de l'utilisateur sur ce post
        // Vérifier flood : on prend le dernier commentaire de l'utilisateur sur ce post
        Comment lastComment = commentRepository.findTopByPostAndAuthoridOrderByDateSendDesc(post, dto.authorid());
        if (lastComment != null && PostValidator.isFlood(lastComment.getDateSend())) {
            throw new RuntimeException("Vous postez trop vite !");
        }


        // Vérifier duplication simple
        boolean duplicate = post.getComments().stream()
                .anyMatch(c -> PostValidator.isDuplicate(c.getContent(), dto.content()));
        if (duplicate) {
            throw new RuntimeException("Commentaire dupliqué !");
        }

        // ✅ Tout est ok, on crée le commentaire
        Comment comment = commentMapper.toEntity(dto);
        comment.setPost(post);
        comment.setDateSend(LocalDateTime.now()); // on ajoute la date

        CommentDTO saved = commentMapper.toDTO(commentRepository.save(comment));

        // ✅ Notifier les abonnés via WebSocket
        messagingTemplate.convertAndSend("/topic/comments/" + postId, saved);

        return saved;
    }

    @Override
    public List<CommentDTO> getCommentsByPost(Long postId) {
        return commentRepository.findByPostIdpost(postId)
                .stream()
                .map(commentMapper::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteComment(Long id) {

        commentRepository.deleteById(id);
        messagingTemplate.convertAndSend("/topic/comments/delete", id);
    }
}
