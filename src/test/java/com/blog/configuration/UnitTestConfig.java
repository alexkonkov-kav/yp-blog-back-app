package com.blog.configuration;

import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import com.blog.service.PostService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.mockito.Mockito.mock;

@Configuration
public class UnitTestConfig {

    @Bean
    public PostService postService(PostRepository postRepository,
                                   TagRepository tagRepository) {
        return new PostService(postRepository, tagRepository);
    }

    @Bean
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }

    @Bean
    public TagRepository tagRepository() {
        return mock(TagRepository.class);
    }
}
