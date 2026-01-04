package com.blog.controller;

import com.blog.service.PostService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

@RestController
@Validated
@RequestMapping("/api/posts")
public class ImageController {

    private final PostService postService;

    public ImageController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping(path = "/{id}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<byte[]> getPostImage(@PathVariable("id") Long id) {
        checkExistsPost(id);

        byte[] bytes = postService.getImageByPostId(id);
        if (bytes == null || bytes.length == 0) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .body(bytes);
    }

    @PutMapping(path = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<String> updatePostImage(@PathVariable("id") Long id, @RequestParam("image") MultipartFile image) throws Exception {
        checkExistsPost(id);

        if (image.isEmpty()) {
            return ResponseEntity.badRequest().body("empty image");
        }
        boolean ok = postService.updateImage(id, image.getBytes());
        if (!ok) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("failed to update image");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body("ok");
    }

    private void checkExistsPost(Long postId) {
        if (!postService.existsPostById(postId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Post not found with id: " + postId);
        }
    }
}
