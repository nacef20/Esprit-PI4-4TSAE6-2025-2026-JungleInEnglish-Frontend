package tn.esprit.forumservice.service;


import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.entites.Reaction;
import tn.esprit.forumservice.entites.ReactionType;
import tn.esprit.forumservice.repository.PostRepo;
import tn.esprit.forumservice.repository.ReactionRepo;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ReactionService {

    private final ReactionRepo reactionRepo;
    private final PostRepo postRepo;
    private final SimpMessagingTemplate messagingTemplate;

    // Ajouter ou modifier une réaction
    public Reaction reactToPost(Long postId, String userId, ReactionType type) {
        Post post = postRepo.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // Vérifie si l'utilisateur a déjà réagi
        Optional<Reaction> existing = reactionRepo.findByPostIdpostAndUserId(postId, userId);

        Reaction reaction;
        if (existing.isPresent()) {
            // Met à jour la réaction
            reaction = existing.get();
            reaction.setType(type);
        } else {
            // Crée une nouvelle réaction
            reaction = new Reaction();
            reaction.setPost(post);
            reaction.setUserId(userId);
            reaction.setType(type);
        }

        Reaction saved = reactionRepo.save(reaction);

        // Notifie en temps réel via WebSocket
        messagingTemplate.convertAndSend("/topic/reactions/" + postId, saved);

        return saved;
    }
    // Supprimer une réaction
    public void deleteReaction(Long postId, String userId) {
        reactionRepo.findByPostIdpostAndUserId(postId, userId).ifPresent(reaction -> {
            reactionRepo.delete(reaction);
            messagingTemplate.convertAndSend("/topic/reactions/delete/" + postId, userId);
        });
    }

    // Récupérer les réactions d’un post
    public List<Reaction> getReactionsByPost(Long postId) {
        return reactionRepo.findByPostIdpost(postId);
    }
}
