package com.blog.controller;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.CreateCommentRequestDto;
import com.blog.dto.comment.UpdateCommentRequestDto;
import com.blog.service.CommentService;
import com.blog.service.PostCommentService;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@Validated
@RequestMapping("/api/posts")
public class CommentController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostCommentService postCommentService;

    public CommentController(CommentService commentService,
                             PostService postService,
                             PostCommentService postCommentService) {
        this.commentService = commentService;
        this.postService = postService;
        this.postCommentService = postCommentService;
    }

    @GetMapping(path = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommentResponseDto> getComments(@PathVariable("id") Long id) {
        return commentService.findCommentsByPostId(id);
    }

    @GetMapping(path = "/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getCommentByPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        checkExistsPost(postId);

        return commentService.getCommentByCommentIdAndPostId(commentId, postId);
    }

    @PostMapping(path = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto addCommentToPost(@PathVariable("postId") Long postId, @Valid @RequestBody CreateCommentRequestDto request) {
        return postCommentService.addCommentToPost(postId, request);
    }

    @PutMapping(path = "/{postId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto updateCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
                                                  @Valid @RequestBody UpdateCommentRequestDto request) {
        checkExistsPost(postId);

        return commentService.updateComment(postId, commentId, request);
    }

    @DeleteMapping("/{postId}/comments/{commentId}")
    @ResponseStatus(HttpStatus.OK)
    public void deleteComment(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        postCommentService.deleteCommentAndDecrementCount(postId, commentId);
    }

    private void checkExistsPost(Long postId) {
        if (!postService.existsPostById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId);
        }
    }
}
