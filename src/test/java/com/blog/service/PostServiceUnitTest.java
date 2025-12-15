package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.mapper.PostMapper;
import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.repository.PostRepository;
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
    private TagService tagService;

    @Autowired
    private PostTagService postTagService;

    @Autowired
    private PostMapper postMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(postRepository, tagService, postTagService, postMapper);
    }

    @Test()
    void testFindById_Success() {
        Post mockPost = new Post(1L, "Test title", "Test text");
        Tag mockTag1 = new Tag(1L, "Test Tag name 1");
        Tag mockTag2 = new Tag(2L, "Test Tag name 2");
        List<Tag> mockTags = Arrays.asList(mockTag1, mockTag2);
        mockPost.setTags(mockTags);
        PostResponseDto dto = new PostResponseDto();
        dto.setId(mockPost.getId());
        dto.setTitle(mockPost.getTitle());
        dto.setText(mockPost.getText());
        dto.setTags(mockPost.getTags().stream().map(Tag::getName).toList());
        dto.setLikesCount(mockPost.getLikesCount());
        dto.setCommentsCount(mockPost.getCommentsCount());
        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.of(mockPost));
        when(tagService.findByPostId(mockPost.getId())).thenReturn(mockTags);
        when(postMapper.mapToResponseDto(mockPost)).thenReturn(dto);

        PostResponseDto resultDto = postService.findById(mockPost.getId());

        assertNotNull(resultDto, "Результат должен быть notNull");
        assertEquals(resultDto.getId(), mockPost.getId(), "Id должен совпадать");
        assertEquals(resultDto.getTitle(), mockPost.getTitle(), "Заголовок должен совпадать");
        assertEquals(resultDto.getTags().size(), mockPost.getTags().size(), "Количество тегов должно совпадать");
        verify(postRepository, times(1)).findById(mockPost.getId());
        verify(tagService, times(1)).findByPostId(mockPost.getId());
    }

    @Test
    void testFindById_NotFound() {
        Post mockPost = new Post(1L, "Test title", "Test text");
        when(postRepository.findById(mockPost.getId())).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> postService.findById(mockPost.getId()),
                "Должно быть new ResponseStatusException с HttpStatus.NOT_FOUND");
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode(), "HttpStatus должен быть NOT_FOUND");
        verify(postRepository, times(1)).findById(mockPost.getId());
        verify(tagService, never()).findByPostId(anyLong());
    }

    @Test()
    void testSave_Success() {
        String title = "Название поста 3";
        String text = "Текст поста в формате Markdown...";
        List<String> tagNames = List.of("tag_1", "tag_2");
        CreatePostRequestDto requestDto = new CreatePostRequestDto(title, text, tagNames);
        Post savedPost = new Post(10L, title, text);
        Tag mockTag1 = new Tag(1L, "tag_1");
        Tag mockTag2 = new Tag(2L, "tag_2");
        List<Tag> foundAndSavedTags = List.of(mockTag1, mockTag2);
        PostResponseDto dto = new PostResponseDto();
        dto.setId(10L);
        dto.setTitle(title);
        dto.setText(text);
        dto.setTags(tagNames);
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        when(tagService.findOrSaveTags(tagNames)).thenReturn(foundAndSavedTags);
        when(postMapper.mapToResponseDto(savedPost)).thenReturn(dto);
        doNothing().when(postTagService).saveLinkPostTag(any(Post.class));
        PostResponseDto resultDto = postService.savePost(requestDto);

        assertNotNull(resultDto, "Результат должен быть notNull");
        assertEquals(dto.getId(), resultDto.getId(), "Id должен совпадать");
        assertEquals(2, resultDto.getTags().size(), "Количество тегов должно совпадать");

        verify(postRepository, times(1)).save(argThat(post ->
                post.getTitle().equals(title) && post.getText().equals(text)));
        verify(tagService, times(1)).findOrSaveTags(tagNames);
        verify(postTagService, times(1)).saveLinkPostTag(savedPost);
        verify(postMapper, times(1)).mapToResponseDto(savedPost);
    }

    @Test
    void testSave_Fails() {
        CreatePostRequestDto requestDto = new CreatePostRequestDto("Название поста 3", "Текст поста в формате Markdown...", List.of("tag_1", "tag_2"));
        Post savedPost = new Post(10L, "Название поста 3", "Текст поста в формате Markdown...");
        when(postRepository.save(any(Post.class))).thenReturn(savedPost);
        RuntimeException mockException = new RuntimeException("Database connection error in TagService");
        when(tagService.findOrSaveTags(anyList())).thenThrow(mockException);
        RuntimeException thrownException = assertThrows(RuntimeException.class,
                () -> postService.savePost(requestDto),
                "Должно быть проброшено исключение из TagService");
        assertEquals(mockException.getMessage(), thrownException.getMessage());

        verify(postRepository, times(1)).save(any(Post.class));
        verify(tagService, times(1)).findOrSaveTags(anyList());
        verify(postTagService, never()).saveLinkPostTag(any(Post.class));
        verify(postMapper, never()).mapToResponseDto(any(Post.class));
    }
}
