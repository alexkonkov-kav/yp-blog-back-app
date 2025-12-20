package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.comment.CommentResponseDto;
import com.blog.model.Comment;
import com.blog.repository.CommentRepository;
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

import java.util.Collections;
import java.util.List;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = UnitTestConfig.class)
@ActiveProfiles("unitTest")
public class CommentServiceUnitTest {

    @Autowired
    private CommentService commentService;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        Mockito.reset(commentRepository);
    }

    @Test
    void testFindCommentsByPostId_Success() {
        Long postId = 1L;
        Comment comment1 = new Comment();
        comment1.setId(10L);
        comment1.setText("Комментарий к посту 1");
        Comment comment2 = new Comment();
        comment2.setId(11L);
        comment2.setText("Ещё один комментарий к посту 1");
        List<Comment> mockComments = List.of(comment1, comment2);
        when(commentRepository.findByPostId(postId)).thenReturn(mockComments);
        List<CommentResponseDto> result = commentService.findCommentsByPostId(postId);
        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Комментарий к посту 1", result.getFirst().getText());
        assertEquals(postId, result.getFirst().getPostId());

        verify(commentRepository, times(1)).findByPostId(postId);
    }

    @Test
    void testFindCommentsByPostId_NotFound() {
        Long postId = 1L;
        when(commentRepository.findByPostId(postId)).thenReturn(Collections.emptyList());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                commentService.findCommentsByPostId(postId)
        );
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("Comment not found for post id: " + postId));
        verify(commentRepository, times(1)).findByPostId(postId);
    }
}
