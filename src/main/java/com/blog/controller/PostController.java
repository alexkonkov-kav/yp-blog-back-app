package com.blog.controller;

import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PagedPostResponseDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
import com.blog.service.PostService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@Validated
@RequestMapping("/api/posts")
public class PostController {

    private final PostService postService;

    public PostController(PostService postService) {
        this.postService = postService;
    }

    @GetMapping()
    public PagedPostResponseDto getSearchPosts(@RequestParam("search") String search,
                                               @RequestParam("pageNumber") int pageNumber,
                                               @RequestParam("pageSize") int pageSize) {
        if (pageNumber < 1) pageNumber = 1;
        if (pageSize < 1) pageSize = 5;
        return postService.getPostsWithPaged(search, pageNumber, pageSize);
    }

    @GetMapping(path = "/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto getPost(@PathVariable("id") Long id) {
        return postService.findById(id);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public PostResponseDto save(@Valid @RequestBody CreatePostRequestDto request) {
        return postService.savePost(request);
    }

    @PostMapping(path = "/{id}/likes", produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public Integer updateLike(@PathVariable("id") Long id) {
        return postService.incrementLikesCount(id);
    }

    @PutMapping(path = "/{id}", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public PostResponseDto update(@PathVariable("id") Long id, @Valid @RequestBody UpdatePostRequestDto request) {
        request.setId(id);
        return postService.updatePost(request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public void delete(@PathVariable("id") Long id) {
        postService.deleteById(id);
    }

}
