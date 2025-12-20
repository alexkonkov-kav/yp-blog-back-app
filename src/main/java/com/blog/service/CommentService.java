package com.blog.service;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;

    public CommentService(CommentRepository commentRepository) {
        this.commentRepository = commentRepository;
    }

    public List<CommentResponseDto> findCommentsByPostId(Long postId) {
        List<Comment> comments = commentRepository.findByPostId(postId);
        if (comments.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment not found for post id: " + postId);
        }
        return comments.stream()
                .map(e -> new CommentResponseDto(e.getId(), e.getText(), postId))
                .toList();
    }
}
