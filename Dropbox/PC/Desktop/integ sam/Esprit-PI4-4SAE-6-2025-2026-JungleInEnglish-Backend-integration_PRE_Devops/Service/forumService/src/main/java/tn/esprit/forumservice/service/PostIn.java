package tn.esprit.forumservice.service;

import tn.esprit.forumservice.dto.PostDTO;
import tn.esprit.forumservice.entites.Post;

import java.util.List;
import java.util.Map;

public interface PostIn {

    PostDTO createPost(PostDTO dto);

    PostDTO updatePost(Long id, PostDTO dto);

    void deletePost(Long id);

    PostDTO getPostById(Long id);

    List<PostDTO> getAllPosts();
    List<Post> getTrendingPosts();

     List<Map<String, Object>> getTop3Posts(int month, int year);
    Map<String, Object> getUser(String userId);
}

