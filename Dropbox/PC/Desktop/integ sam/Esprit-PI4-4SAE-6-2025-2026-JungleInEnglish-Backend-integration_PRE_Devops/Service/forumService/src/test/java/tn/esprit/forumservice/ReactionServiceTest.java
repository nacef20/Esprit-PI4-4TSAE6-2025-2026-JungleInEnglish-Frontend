package tn.esprit.forumservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.entites.Reaction;
import tn.esprit.forumservice.entites.ReactionType;
import tn.esprit.forumservice.repository.PostRepo;
import tn.esprit.forumservice.repository.ReactionRepo;
import tn.esprit.forumservice.service.ReactionService;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReactionServiceTest {

    @Mock
    private ReactionRepo reactionRepo;

    @Mock
    private PostRepo postRepo;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private ReactionService reactionService;

    // ===================== NEW REACTION =====================
    @Test
    void shouldCreateNewReaction() {

        Post post = new Post();

        when(postRepo.findById(1L)).thenReturn(Optional.of(post));
        when(reactionRepo.findByPostIdpostAndUserId(1L, "u1"))
                .thenReturn(Optional.empty());

        Reaction reaction = new Reaction();
        reaction.setUserId("u1");
        reaction.setType(ReactionType.LIKE);

        when(reactionRepo.save(any())).thenReturn(reaction);

        Reaction result = reactionService.reactToPost(1L, "u1", ReactionType.LIKE);

        assertNotNull(result);
        assertEquals("u1", result.getUserId());
        assertEquals(ReactionType.LIKE, result.getType());

        verify(reactionRepo, times(1)).save(any());
    }

    // ===================== POST NOT FOUND =====================
    @Test
    void shouldThrowExceptionWhenPostNotFound() {

        when(postRepo.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> reactionService.reactToPost(99L, "u1", ReactionType.LIKE));
    }

    // ===================== DELETE REACTION =====================
    @Test
    void shouldDeleteReactionSuccessfully() {

        Reaction reaction = new Reaction();

        when(reactionRepo.findByPostIdpostAndUserId(1L, "u1"))
                .thenReturn(Optional.of(reaction));

        reactionService.deleteReaction(1L, "u1");

        verify(reactionRepo).delete(reaction);

    }

    // ===================== GET REACTIONS =====================
    @Test
    void shouldReturnReactionsByPost() {

        Reaction reaction = new Reaction();

        when(reactionRepo.findByPostIdpost(1L))
                .thenReturn(List.of(reaction));

        List<Reaction> result = reactionService.getReactionsByPost(1L);

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());

        verify(reactionRepo).findByPostIdpost(1L);
    }

    // ===================== EMPTY CASE =====================
    @Test
    void shouldReturnEmptyListWhenNoReactions() {

        when(reactionRepo.findByPostIdpost(1L))
                .thenReturn(List.of());

        List<Reaction> result = reactionService.getReactionsByPost(1L);

        assertTrue(result.isEmpty());
    }
}