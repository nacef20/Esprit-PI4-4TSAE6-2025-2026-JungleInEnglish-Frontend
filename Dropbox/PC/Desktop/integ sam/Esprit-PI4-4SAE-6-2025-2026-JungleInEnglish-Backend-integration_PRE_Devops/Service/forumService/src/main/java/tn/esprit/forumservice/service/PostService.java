package tn.esprit.forumservice.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import tn.esprit.forumservice.dto.PostDTO;
import tn.esprit.forumservice.entites.Post;
import tn.esprit.forumservice.mapper.PostMapper;
import tn.esprit.forumservice.repository.PostRepo;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService implements PostIn {

    private final PostRepo postRepository;
    private final PostMapper postMapper;
    private final UserClient userClient;

    @Override
    public Map<String, Object> getUser(String userId) {
        return userClient.getUserById(userId);
    }


    @Override
    public PostDTO createPost(PostDTO dto) {
        Post post = postMapper.toEntity(dto);
        return postMapper.toDTO(postRepository.save(post));
    }

    @Override
    public PostDTO updatePost(Long id, PostDTO dto) {
        Post existing = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        existing.setTitle(dto.title());
        existing.setContent(dto.content());

        return postMapper.toDTO(postRepository.save(existing));
    }

    @Override
    public void deletePost(Long id) {
        postRepository.deleteById(id);
    }

    @Override
    public PostDTO getPostById(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postMapper.toDTO(post);
    }

    @Override
    public List<PostDTO> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(postMapper::toDTO)
                .collect(Collectors.toList());
    }

    public List<Post> getTrendingPosts() {
        return postRepository.findTrendingPosts();
    }



    public List<Map<String, Object>> getTop3Posts(int month, int year) {
        return postRepository.findTop3Posts(month, year);
    }

}
