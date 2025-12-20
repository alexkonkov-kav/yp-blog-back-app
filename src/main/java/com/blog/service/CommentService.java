package com.blog.service;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.UpdateCommentRequestDto;
import com.blog.mapper.CommentMapper;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public CommentService(CommentRepository commentRepository,
                          CommentMapper commentMapper) {
        this.commentRepository = commentRepository;
        this.commentMapper = commentMapper;
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

    public CommentResponseDto getCommentByCommentIdAndPostId(Long commentId, Long postId) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment id: " + commentId + " not found for post id: " + postId));
        return commentMapper.mapToResponse(comment, postId);
    }

    public CommentResponseDto updateComment(Long postId, Long commentId, UpdateCommentRequestDto request) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Comment id: " + commentId + " not found for post id: " + postId));
        comment.setText(request.getText());
        commentRepository.update(comment);
        return commentMapper.mapToResponse(comment, postId);
    }
}
