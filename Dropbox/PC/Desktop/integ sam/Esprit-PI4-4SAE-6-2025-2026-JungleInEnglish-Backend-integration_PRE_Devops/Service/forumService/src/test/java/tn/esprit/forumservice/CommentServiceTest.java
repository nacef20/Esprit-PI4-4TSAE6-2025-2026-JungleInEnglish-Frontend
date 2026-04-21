package tn.esprit.forumservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import tn.esprit.forumservice.dto.CommentDTO;
import tn.esprit.forumservice.entites.Comment;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.mapper.CommentMapper;
import tn.esprit.forumservice.repository.ComentRepo;
import tn.esprit.forumservice.repository.PostRepo;
import tn.esprit.forumservice.service.CommentService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommentServiceTest {

    @Mock
    private ComentRepo commentRepository;

    @Mock
    private PostRepo postRepository;

    @Mock
    private CommentMapper commentMapper;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @InjectMocks
    private CommentService commentService;

    // ===================== CREATE COMMENT =====================
    @Test
    void shouldCreateCommentSuccessfully() {

        // GIVEN
        Post post = new Post();
        post.setComments(List.of());

        CommentDTO dto = new CommentDTO(
                1L,
                "hello world",
                "user1",
                LocalDateTime.now()
        );

        Comment comment = new Comment();

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(commentMapper.toEntity(dto)).thenReturn(comment);
        when(commentRepository.save(comment)).thenReturn(comment);
        when(commentMapper.toDTO(comment)).thenReturn(dto);

        // WHEN
        CommentDTO result = commentService.createComment(1L, dto);

        // THEN
        assertNotNull(result);
        assertEquals("hello world", result.content());
        assertEquals("user1", result.authorid());

        verify(commentRepository, times(1)).save(comment);
        verify(postRepository, times(1)).findById(1L);
    }

    // ===================== POST NOT FOUND =====================
    @Test
    void shouldThrowExceptionWhenPostNotFound() {

        CommentDTO dto = new CommentDTO(1L, "hello", "user1", LocalDateTime.now());

        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> commentService.createComment(99L, dto));
    }

    // ===================== GET COMMENTS =====================
    @Test
    void shouldReturnCommentsByPost() {

        Comment comment = new Comment();
        CommentDTO dto = mock(CommentDTO.class);

        when(commentRepository.findByPostIdpost(1L)).thenReturn(List.of(comment));
        when(commentMapper.toDTO(comment)).thenReturn(dto);

        List<CommentDTO> result = commentService.getCommentsByPost(1L);

        assertEquals(1, result.size());
        verify(commentRepository).findByPostIdpost(1L);
    }

    // ===================== EMPTY COMMENTS =====================
    @Test
    void shouldReturnEmptyListWhenNoComments() {

        when(commentRepository.findByPostIdpost(1L)).thenReturn(List.of());

        List<CommentDTO> result = commentService.getCommentsByPost(1L);

        assertTrue(result.isEmpty());
    }

    // ===================== DELETE COMMENT =====================
    @Test
    void shouldDeleteCommentAndSendNotification() {

        commentService.deleteComment(1L);

        verify(commentRepository).deleteById(1L);
        verify(messagingTemplate)
                .convertAndSend("/topic/comments/delete", 1L);
    }
}