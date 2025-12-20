package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.CreateCommentRequestDto;
import com.blog.mapper.CommentMapper;
import com.blog.model.Comment;
import com.blog.model.Post;
import com.blog.repository.CommentRepository;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UnitTestConfig.class)
@ActiveProfiles("unitTest")
public class PostCommentServiceUnitTest {

    @Autowired
    private PostCommentService postCommentService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(postRepository, commentRepository, commentMapper);
    }

    @Test
    void addCommentToPost_Success() {
        Long postId = 1L;
        CreateCommentRequestDto requestDto = new CreateCommentRequestDto("Комментарий к посту", postId);
        Post mockPost = new Post(postId, "Название поста 1", "Текст поста в формате Markdown...");
        Comment savedComment = new Comment(requestDto.getText(), mockPost);
        savedComment.setId(100L);
        CommentResponseDto expectedResponse = new CommentResponseDto(savedComment.getId(), savedComment.getText(), postId);
        when(postRepository.findById(postId)).thenReturn(Optional.of(mockPost));
        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);
        when(commentMapper.mapToResponse(savedComment, postId)).thenReturn(expectedResponse);
        CommentResponseDto result = postCommentService.addCommentToPost(postId, requestDto);
        assertNotNull(result);
        assertEquals(requestDto.getText(), result.getText());
        assertEquals(postId, result.getPostId());
        verify(postRepository).findById(postId);
        verify(commentRepository).save(argThat(comment ->
                comment.getText().equals(requestDto.getText()) && comment.getPost().equals(mockPost)));
        verify(postRepository).incrementCommentsCount(postId);
        verify(commentMapper).mapToResponse(savedComment, postId);
    }

    @Test
    void addCommentToPost_PostNotFound() {
        Long postId = 999L;
        CreateCommentRequestDto requestDto = new CreateCommentRequestDto("Комментарий к посту", postId);
        when(postRepository.findById(postId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> postCommentService.addCommentToPost(postId, requestDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(commentRepository, never()).save(any());
        verify(postRepository, never()).incrementCommentsCount(anyLong());
    }
}
