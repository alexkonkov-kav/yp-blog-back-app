package com.blog.configuration;

import com.blog.mapper.PostMapper;
import com.blog.repository.CommentRepository;
import com.blog.repository.PostRepository;
import com.blog.service.CommentService;
import com.blog.service.PostService;
import com.blog.service.PostTagService;
import com.blog.service.TagService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("unitTest")
public class UnitTestConfig {

    @Bean
    public PostService postService(PostRepository postRepository,
                                   TagService tagService,
                                   PostTagService postTagService,
                                   PostMapper postMapper) {
        return new PostService(postRepository, tagService, postTagService, postMapper);
    }

    @Bean
    public CommentService commentService(CommentRepository commentRepository) {
        return new CommentService(commentRepository);
    }

    @Bean
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }

    @Bean
    public TagService tagService() {
        return mock(TagService.class);
    }

    @Bean
    public PostTagService postTagService() {
        return mock(PostTagService.class);
    }

    @Bean
    public PostMapper postMapper() {
        return mock(PostMapper.class);
    }

    @Bean
    public CommentRepository commentRepository() {
        return mock(CommentRepository.class);
    }
}
