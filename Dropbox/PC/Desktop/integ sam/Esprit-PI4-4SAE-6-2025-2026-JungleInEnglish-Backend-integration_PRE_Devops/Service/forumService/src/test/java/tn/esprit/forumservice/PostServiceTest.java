package tn.esprit.forumservice;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tn.esprit.forumservice.dto.PostDTO;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.mapper.PostMapper;
import tn.esprit.forumservice.repository.PostRepo;
import tn.esprit.forumservice.service.PostService;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PostServiceTest {

    @Mock
    private PostRepo postRepository;

    @Mock
    private PostMapper postMapper;

    @InjectMocks
    private PostService postService;

    // ===================== CREATE =====================
    @Test
    void shouldCreatePostSuccessfully() {
        // GIVEN
        PostDTO dto = new PostDTO(1L, "title", "content", "user1", null, null, null);
        Post postEntity = new Post();

        when(postMapper.toEntity(dto)).thenReturn(postEntity);
        when(postRepository.save(postEntity)).thenReturn(postEntity);
        when(postMapper.toDTO(postEntity)).thenReturn(dto);

        // WHEN
        PostDTO result = postService.createPost(dto);

        // THEN
        assertNotNull(result);
        assertEquals("title", result.title());
        assertEquals("content", result.content());
        verify(postRepository, times(1)).save(postEntity);
    }

    // ===================== UPDATE =====================
    @Test
    void shouldUpdatePostWhenExists() {
        Post existing = new Post();
        PostDTO dto = new PostDTO(1L, "new title", "new content", "user1", null, null, null);

        when(postRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(postRepository.save(existing)).thenReturn(existing);
        when(postMapper.toDTO(existing)).thenReturn(dto);

        PostDTO result = postService.updatePost(1L, dto);

        assertNotNull(result);
        assertEquals("new title", result.title());
        verify(postRepository).findById(1L);
        verify(postRepository).save(existing);
    }

    // ===================== UPDATE NOT FOUND =====================
    @Test
    void shouldThrowExceptionWhenPostNotFound() {
        when(postRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> postService.updatePost(99L, new PostDTO(99L, "t", "c", "u", null, null, null)));
    }

    // ===================== DELETE =====================
    @Test
    void shouldDeletePost() {
        postService.deletePost(1L);

        verify(postRepository, times(1)).deleteById(1L);
    }

    // ===================== GET BY ID =====================
    @Test
    void shouldReturnPostById() {
        Post post = new Post();
        PostDTO dto = new PostDTO(1L, "title", "content", "user1", null, null, null);

        when(postRepository.findById(1L)).thenReturn(Optional.of(post));
        when(postMapper.toDTO(post)).thenReturn(dto);

        PostDTO result = postService.getPostById(1L);

        assertEquals("title", result.title());
    }

    // ===================== GET ALL =====================
    @Test
    void shouldReturnAllPosts() {
        Post post = new Post();
        PostDTO dto = new PostDTO(1L, "title", "content", "user1", null, null, null);

        when(postRepository.findAll()).thenReturn(List.of(post));
        when(postMapper.toDTO(post)).thenReturn(dto);

        List<PostDTO> result = postService.getAllPosts();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
    }

    // ===================== TRENDING =====================
    @Test
    void shouldReturnTrendingPosts() {
        when(postRepository.findTrendingPosts()).thenReturn(List.of(new Post()));

        List<Post> result = postService.getTrendingPosts();

        assertEquals(1, result.size());
    }

    // ===================== TOP POSTS =====================
    @Test
    void shouldReturnTopPosts() {
        Map<String, Object> map = new HashMap<>();
        map.put("idpost", 1L);
        map.put("score", 100);

        when(postRepository.findTop3Posts(4, 2026)).thenReturn(List.of(map));

        List<Map<String, Object>> result = postService.getTop3Posts(4, 2026);

        assertNotNull(result);
        assertEquals(100, result.get(0).get("score"));
    }
}