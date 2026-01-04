package com.blog.service;

import com.blog.dto.SearchParamDto;
import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PagedPostResponseDto;
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
    private final SearchParamService searchParamService;

    public PostService(PostRepository postRepository,
                       TagService tagService,
                       PostTagService postTagService,
                       PostMapper postMapper,
                       SearchParamService searchParamService) {
        this.postRepository = postRepository;
        this.tagService = tagService;
        this.postTagService = postTagService;
        this.postMapper = postMapper;
        this.searchParamService = searchParamService;
    }

    public PostResponseDto findById(Long id) {
        Post post = postRepository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Post with id: " + id + " not found"));
        post.setTags(tagService.findByPostId(id));
        return postMapper.mapToResponseDto(post);
    }

    public PostResponseDto savePost(CreatePostRequestDto request) {
        Post post = postRepository.save(new Post(request.getTitle(), request.getText()));
        List<Tag> tags = tagService.findOrSaveTags(request.getTags());
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

    public byte[] getImageByPostId(Long id) {
        return postRepository.findImageById(id);
    }

    public PagedPostResponseDto getPostsWithPaged(String search, int pageNumber, int pageSize) {
        SearchParamDto searchParamDto = searchParamService.parseSearchParam(search);
        int offset = (pageNumber - 1) * pageSize;

        List<Post> posts = postRepository
                .findAll(searchParamDto.getSearchText(), searchParamDto.getTagNames(), pageSize, offset);
        long totalElements = postRepository
                .count(searchParamDto.getSearchText(), searchParamDto.getTagNames());

        long totalPages = (long) Math.ceil((double) totalElements / pageSize);
        if (totalPages == 0) totalPages = 1;

        List<PostResponseDto> postResponses = posts.stream().map(post -> {
            post.setText(formatPostText(post.getText()));
            post.setTags(tagService.findByPostId(post.getId()));
            return postMapper.mapToResponseDto(post);
        }).toList();

        boolean hasPrev = pageNumber > 1;
        boolean hasNext = pageNumber < totalPages;

        return new PagedPostResponseDto(postResponses, hasPrev, hasNext, totalPages);
    }

    private String formatPostText(String text) {
        if (text == null) return "";

        if (text.length() > 128) {
            return text.substring(0, 128) + "...";
        }
        return text;
    }
}
