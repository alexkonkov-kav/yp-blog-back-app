package com.blog.service;

import com.blog.dto.post.PostResponseDto;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final TagRepository tagRepository;

    public PostService(PostRepository postRepository,
                       TagRepository tagRepository) {
        this.postRepository = postRepository;
        this.tagRepository = tagRepository;
    }

    public PostResponseDto findById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + id + " not found"));
        post.setTags(tagRepository.findByPostId(id));
        return mapToResponseDto(post);
    }

    private PostResponseDto mapToResponseDto(Post post) {
        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setId(post.getId());
        postResponseDto.setTitle(post.getTitle());
        postResponseDto.setText(post.getText());
        postResponseDto.setTags(post.getTags().stream().map(Tag::getName).toList());
        postResponseDto.setLikesCount(post.getLikesCount());
        postResponseDto.setCommentsCount(post.getCommentsCount());
        return postResponseDto;
    }
}
