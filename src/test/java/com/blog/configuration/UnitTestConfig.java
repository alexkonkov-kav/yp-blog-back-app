package com.blog.configuration;

import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import com.blog.service.PostService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

import static org.mockito.Mockito.mock;

@Configuration
@Profile("unitTest")
public class UnitTestConfig {

    @Bean
    public PostService postService(@Qualifier("postRepository") PostRepository postRepository,
                                   @Qualifier("tagRepository") TagRepository tagRepository) {
        return new PostService(postRepository, tagRepository);
    }

    @Bean
    @Primary
    public PostRepository postRepository() {
        return mock(PostRepository.class);
    }

    @Bean
    @Primary
    public TagRepository tagRepository() {
        return mock(TagRepository.class);
    }
}
