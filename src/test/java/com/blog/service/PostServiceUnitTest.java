package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.post.PostResponseDto;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.repository.PostRepository;
import com.blog.repository.TagRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UnitTestConfig.class)
@ActiveProfiles("unitTest")
public class PostServiceUnitTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(postRepository, tagRepository);
    }

    @Test()
    void testFindById_Success() {
        Post mockPost = new Post(1L, "Test title", "Test text");
        Tag mockTag1 = new Tag(1L, "Test Tag name 1");
        Tag mockTag2 = new Tag(2L, "Test Tag name 2");
        List<Tag> mockTags = Arrays.asList(mockTag1, mockTag2);
        mockPost.setTags(mockTags);
        Mockito.when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        Mockito.when(tagRepository.findByPostId(mockPost.getId())).thenReturn(mockTags);

        PostResponseDto resultDto = postService.findById(mockPost.getId());
        assertNotNull(resultDto, "Результат должен быть notNull");
        assertEquals(resultDto.getId(), mockPost.getId(), "Id должен совпадать");
        assertEquals(resultDto.getTitle(), mockPost.getTitle(), "Заголовок должен совпадать");
        assertEquals(resultDto.getTags().size(), mockPost.getTags().size(), "Количество тегов должно совпадать");
        verify(postRepository, times(1)).findById(mockPost.getId());
        verify(tagRepository, times(1)).findByPostId(mockPost.getId());
    }

    @Test
    void testFindById_NotFound() {
        Post mockPost = new Post(1L, "Test title", "Test text");
        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> postService.findById(mockPost.getId()),
                "Должно быть new ResponseStatusException с HttpStatus.NOT_FOUND");
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "HttpStatus должен быть NOT_FOUND");
        verify(postRepository, times(1)).findById(mockPost.getId());
        verify(tagRepository, never()).findByPostId(anyLong());
    }
}
