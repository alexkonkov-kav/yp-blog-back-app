package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
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
import java.util.Collections;
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

    @Test
    void testUpdatePost_Success() {
        Long postId = 1L;
        List<String> newTagNames = List.of("tag_1", "tag_2");
        UpdatePostRequestDto requestDto = new UpdatePostRequestDto();
        requestDto.setId(postId);
        requestDto.setTitle("Название поста 1");
        requestDto.setText("Текст поста в формате Markdown...");
        requestDto.setTags(newTagNames);
        Post existingPost = new Post(postId, "Old Title", "Old Text");
        List<Tag> mockTags = List.of(new Tag(1L, "tag_1"), new Tag(2L, "tag_1"));
        PostResponseDto responseDto = new PostResponseDto();
        responseDto.setId(postId);
        responseDto.setTitle(requestDto.getTitle());
        responseDto.setText(requestDto.getText());
        responseDto.setTags(newTagNames);

        when(tagService.findOrSaveTags(newTagNames)).thenReturn(mockTags);
        when(postRepository.findById(postId)).thenReturn(Optional.of(existingPost));
        when(postMapper.mapToResponseDto(any(Post.class))).thenReturn(responseDto);
        PostResponseDto resultDto = postService.updatePost(requestDto);
        assertNotNull(resultDto);
        assertEquals("Название поста 1", resultDto.getTitle());
        verify(postRepository, times(1)).update(argThat(post ->
                post.getId().equals(postId) &&
                post.getTitle().equals(requestDto.getTitle()) &&
                post.getText().equals(requestDto.getText())
        ));

        verify(tagService, times(1)).findOrSaveTags(newTagNames);
        verify(postTagService, times(1)).saveLinkPostTag(any(Post.class));
        verify(postMapper, times(1)).mapToResponseDto(any(Post.class));
    }

    @Test
    void testUpdatePost_NotFound() {
        Long postId = 999L;
        UpdatePostRequestDto requestDto = new UpdatePostRequestDto();
        requestDto.setId(postId);
        requestDto.setTags(List.of("tag_1"));
        when(tagService.findOrSaveTags(anyList())).thenReturn(Collections.emptyList());
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                postService.updatePost(requestDto)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(postRepository, never()).update(any());
        verify(postTagService, never()).saveLinkPostTag(any());
    }

    @Test
    void testIncrementLikesCount_Success() {
        Long postId = 1L;
        int currentLikes = 10;
        Post mockPost = new Post(postId, "Название поста 1", "Текст поста в формате Markdown...");
        mockPost.setLikesCount(currentLikes);
        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        Integer newLikesCount = postService.incrementLikesCount(postId);
        assertEquals(currentLikes + 1, newLikesCount, "Метод должен вернуть Инкремент числа лайков поста");
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, times(1)).incrementLikesCount(postId);
    }

    @Test
    void testIncrementLikesCount_NotFound() {
        Long postId = 999L;
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                postService.incrementLikesCount(postId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(postRepository, times(1)).findById(postId);
        verify(postRepository, never()).incrementLikesCount(anyLong());
    }

    @Test
    void testDeleteById_Success() {
        Long postId = 1L;
        postService.deleteById(postId);
        verify(postRepository, times(1)).deleteById(postId);
    }

    @Test
    void testExistsPostById_True() {
        Long postId = 1L;
        when(postRepository.existsById(postId)).thenReturn(true);
        boolean exists = postService.existsPostById(postId);
        assertTrue(exists, "Post with id: " + postId + " is found");
        verify(postRepository, times(1)).existsById(postId);
    }

    @Test
    void testExistsPostById_False() {
        Long postId = 999L;
        when(postRepository.existsById(postId)).thenReturn(false);
        boolean exists = postService.existsPostById(postId);
        assertFalse(exists, "Post with id: " + postId + " not found");
        verify(postRepository, times(1)).existsById(postId);
    }

    @Test
    void testUpdateImage_Success() {
        Long postId = 1L;
        byte[] imageContent = new byte[]{1, 2, 3, 4};
        when(postRepository.updateImage(postId, imageContent)).thenReturn(true);
        boolean result = postService.updateImage(postId, imageContent);
        assertTrue(result, "Image is updated");
        verify(postRepository, times(1)).updateImage(postId, imageContent);
    }

    @Test
    void testGetImageByPostId_Success() {
        Long postId = 1L;
        byte[] expectedImage = new byte[]{(byte) 137, 80, 78, 71}; // Stub PNG header
        when(postRepository.findImageById(postId)).thenReturn(expectedImage);
        byte[] resultImage = postService.getImageByPostId(postId);

        assertNotNull(resultImage, "Массив байтов не должен быть null");
        assertArrayEquals(expectedImage, resultImage, "Массив байт должен совпадать");
        verify(postRepository, times(1)).findImageById(postId);
    }

    @Test
    void testGetImageByPostId_NotFound() {
        Long postId = 999L;
        when(postRepository.findImageById(postId)).thenReturn(null);
        byte[] resultImage = postService.getImageByPostId(postId);

        assertNull(resultImage, "Метод должен вернуть null");
        verify(postRepository, times(1)).findImageById(postId);
    }
}
