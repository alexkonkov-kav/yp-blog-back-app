package com.blog.controller;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.CreateCommentRequestDto;
import com.blog.dto.comment.UpdateCommentRequestDto;
import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
import com.blog.service.CommentService;
import com.blog.service.PostCommentService;
import com.blog.service.PostService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;
    private final CommentService commentService;
    private final PostCommentService postCommentService;

    public PostController(PostService postService,
                          CommentService commentService,
                          PostCommentService postCommentService) {
        this.postService = postService;
        this.commentService = commentService;
        this.postCommentService = postCommentService;
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto getPost(@PathVariable("id") Long id) {
        return postService.findById(id);
    }

    @GetMapping(path = "/{id}/comments", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<CommentResponseDto> getComments(@PathVariable("id") Long id) {
        return commentService.findCommentsByPostId(id);
    }

    @GetMapping(path = "/{postId}/comments/{commentId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto getCommentByPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId) {
        if (!postService.existsPostById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId);
        }
        return commentService.getCommentByCommentIdAndPostId(commentId, postId);
    }

    @GetMapping(path = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPostImage(@PathVariable("id") Long id) {
        if (!postService.existsPostById(id)) {
            return ResponseEntity.notFound().build();
        }
        byte[] bytes = postService.getImageByPostId(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(bytes);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDto save(@RequestBody CreatePostRequestDto request) {
        return postService.savePost(request);
    }

    @PostMapping(path = "/{id}/likes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Integer updateLike(@PathVariable("id") Long id) {
        return postService.incrementLikesCount(id);
    }

    @PostMapping(path = "/{postId}/comments", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentResponseDto addCommentToPost(@PathVariable("postId") Long postId, @RequestBody CreateCommentRequestDto request) {
        return postCommentService.addCommentToPost(postId, request);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto update(@PathVariable("id") Long id, @RequestBody UpdatePostRequestDto request) {
        request.setId(id);
        return postService.updatePost(request);
    }

    @PutMapping(path = "/{postId}/comments/{commentId}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CommentResponseDto updateCommentToPost(@PathVariable("postId") Long postId, @PathVariable("commentId") Long commentId,
                                                  @RequestBody UpdateCommentRequestDto request) {
        if (!postService.existsPostById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId);
        }
        return commentService.updateComment(postId, commentId, request);
    }

    @PutMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePostImage(@PathVariable("id") Long id, @RequestParam("image") MultipartFile image) throws Exception {
        if (!postService.existsPostById(id)) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("post not found");
        }
        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("empty image");
        }
        boolean ok = postService.updateImage(id, image.getBytes());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to update image");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        postService.deleteById(id);
    }
}
