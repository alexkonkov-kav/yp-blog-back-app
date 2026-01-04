package com.blog.service;

import com.blog.configuration.UnitTestConfig;
import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.UpdateCommentRequestDto;
import com.blog.mapper.CommentMapper;
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

import java.util.List;
import java.util.Objects;
import java.util.Optional;

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

    @Autowired
    private CommentMapper commentMapper;

    @BeforeEach
    void setUp() {
        Mockito.reset(commentRepository, commentMapper);
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
    void testGetCommentByCommentIdAndPostId_Success() {
        Long postId = 1L;
        Comment mockComment = new Comment();
        mockComment.setId(10L);
        mockComment.setText("Test comment text");
        CommentResponseDto expectedDto = new CommentResponseDto(mockComment.getId(), mockComment.getText(), postId);
        when(commentRepository.findByIdAndPostId(mockComment.getId(), postId)).thenReturn(Optional.of(mockComment));
        when(commentMapper.mapToResponse(mockComment, postId)).thenReturn(expectedDto);
        CommentResponseDto resultDto = commentService.getCommentByCommentIdAndPostId(mockComment.getId(), postId);
        assertNotNull(resultDto);
        assertEquals(mockComment.getId(), resultDto.getId());
        assertEquals(mockComment.getText(), resultDto.getText());
        assertEquals(postId, resultDto.getPostId());
        verify(commentRepository, times(1)).findByIdAndPostId(mockComment.getId(), postId);
        verify(commentMapper, times(1)).mapToResponse(mockComment, postId);
    }

    @Test
    void testGetCommentByCommentIdAndPostId_NotFound() {
        Long postId = 1L;
        Long commentId = 999L;
        when(commentRepository.findByIdAndPostId(commentId, postId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                commentService.getCommentByCommentIdAndPostId(commentId, postId));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertTrue(Objects.requireNonNull(exception.getReason()).contains("not found for post id: " + postId));
        verify(commentMapper, never()).mapToResponse(any(), anyLong());
    }

    @Test
    void testUpdateComment_Success() {
        Long postId = 1L;
        UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto(2L, "Второй комментарий к посту 1", postId);
        Comment existingComment = new Comment();
        existingComment.setId(requestDto.getId());
        existingComment.setText("Старый текст");
        CommentResponseDto expectedDto = new CommentResponseDto(existingComment.getId(), requestDto.getText(), postId);
        when(commentRepository.findByIdAndPostId(existingComment.getId(), postId)).thenReturn(Optional.of(existingComment));
        when(commentMapper.mapToResponse(existingComment, postId)).thenReturn(expectedDto);
        CommentResponseDto resultDto = commentService.updateComment(postId, requestDto.getId(), requestDto);
        assertNotNull(resultDto);
        assertEquals(requestDto.getText(), resultDto.getText());
        verify(commentRepository, times(1)).findByIdAndPostId(requestDto.getId(), postId);
        verify(commentRepository, times(1)).update(existingComment);
        verify(commentMapper, times(1)).mapToResponse(existingComment, postId);
    }

    @Test
    void testUpdateComment_NotFound() {
        Long postId = 1L;
        UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto(999L, "Второй комментарий к посту 1", postId);
        when(commentRepository.findByIdAndPostId(requestDto.getId(), postId)).thenReturn(Optional.empty());
        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () ->
                commentService.updateComment(postId, requestDto.getId(), requestDto));
        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(commentRepository, never()).update(any());
        verify(commentMapper, never()).mapToResponse(any(), anyLong());
    }
}
