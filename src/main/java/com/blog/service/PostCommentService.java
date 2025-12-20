package com.blog.service;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.CreateCommentRequestDto;
import com.blog.mapper.CommentMapper;
import com.blog.model.Comment;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostCommentService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final CommentMapper commentMapper;

    public PostCommentService(CommentMapper commentMapper,
                              PostRepository postRepository,
                              CommentRepository commentRepository) {
        this.commentMapper = commentMapper;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    public CommentResponseDto addCommentToPost(Long postId, CreateCommentRequestDto request) {
        Post post = postRepository.findById(postId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + postId + "not found"));
        Comment comment = commentRepository.save(new Comment(request.getText(), post));
        postRepository.incrementCommentsCount(postId);
        return commentMapper.mapToResponse(comment, postId);
    }
}
