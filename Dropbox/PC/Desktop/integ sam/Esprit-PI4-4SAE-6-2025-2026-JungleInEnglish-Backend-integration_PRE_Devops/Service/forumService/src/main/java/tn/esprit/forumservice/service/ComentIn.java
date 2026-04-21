package tn.esprit.forumservice.service;

import tn.esprit.forumservice.dto.CommentDTO;

import java.util.List;

public interface ComentIn {

    CommentDTO createComment(Long postId, CommentDTO dto);

    List<CommentDTO> getCommentsByPost(Long postId);

    void deleteComment(Long id);
}
