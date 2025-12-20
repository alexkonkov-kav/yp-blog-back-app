package com.blog.controller;

import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
import com.blog.service.PostService;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto getPost(@PathVariable("id") Long id) {
        return postService.findById(id);
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

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto update(@PathVariable("id") Long id, @RequestBody UpdatePostRequestDto request) {
        request.setId(id);
        return postService.updatePost(request);
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
