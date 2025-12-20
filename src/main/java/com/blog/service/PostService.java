package com.blog.service;

import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
import com.blog.mapper.PostMapper;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.repository.PostRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagService tagService;
    private final PostTagService postTagService;
    private final PostMapper postMapper;

    public PostService(PostRepository postRepository,
                       TagService tagService,
                       PostTagService postTagService,
                       PostMapper postMapper) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.postTagService = postTagService;
        this.postMapper = postMapper;
    }

    public PostResponseDto findById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + id + " not found"));
        post.setTags(tagService.findByPostId(id));
        return postMapper.mapToResponseDto(post);
    }

    public PostResponseDto savePost(CreatePostRequestDto request) {
        Post post = postRepository.save(new Post(request.title(), request.text()));
        List<Tag> tags = tagService.findOrSaveTags(request.tags());
        post.setTags(tags);
        postTagService.saveLinkPostTag(post);
        return postMapper.mapToResponseDto(post);
    }

    public PostResponseDto updatePost(UpdatePostRequestDto request) {
        List<Tag> tags = tagService.findOrSaveTags(request.getTags());
        Post post = postRepository.findById(request.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + request.getId() + "not found"));
        post.setTitle(request.getTitle());
        post.setText(request.getText());
        postRepository.update(post);
        post.setTags(tags);
        postTagService.saveLinkPostTag(post);
        return postMapper.mapToResponseDto(post);
    }

    public Integer incrementLikesCount(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + id + "not found"));
        postRepository.incrementLikesCount(id);
        return post.getLikesCount() + 1;
    }

    public void deleteById(Long postId) {
        postRepository.deleteById(postId);
    }

    public boolean existsPostById(Long id) {
        return postRepository.existsById(id);
    }

    public boolean updateImage(Long id, byte[] image) {
        return postRepository.updateImage(id, image);
    }
}
