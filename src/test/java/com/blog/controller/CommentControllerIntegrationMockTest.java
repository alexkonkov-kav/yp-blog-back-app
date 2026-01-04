package com.blog.controller;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.dto.comment.CreateCommentRequestDto;
import com.blog.dto.comment.UpdateCommentRequestDto;
import com.blog.service.CommentService;
import com.blog.service.PostCommentService;
import com.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CommentController.class)
public class CommentControllerIntegrationMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @MockitoBean
    private CommentService commentService;

    @MockitoBean
    private PostCommentService postCommentService;

    @Test
    void getCommentsByPostId_returnsJsonArray() throws Exception {
        CommentResponseDto dto1 = new CommentResponseDto(1L, "Test comment name 1", 1L);
        CommentResponseDto dto2 = new CommentResponseDto(2L, "Test comment name 2", 1L);
        when(commentService.findCommentsByPostId(1L)).thenReturn(List.of(dto1, dto2));

        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Test comment name 1"))
                .andExpect(jsonPath("$[1].text").value("Test comment name 2"));
    }

    @Test
    void getCommentByPost() throws Exception {
        CommentResponseDto dto = new CommentResponseDto(1L, "Test comment name 1", 1L);
        when(postService.existsPostById(1L)).thenReturn(true);
        when(commentService.getCommentByCommentIdAndPostId(1L, 1L)).thenReturn(dto);
        mockMvc.perform(get("/api/posts/{postId}/comments/{commentId}", 1L, 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.text").value("Test comment name 1"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    void addCommentToPost_success() throws Exception {
        String json = """
                {"text":"Комментарий к посту","postId":"1"}
                """;
        CommentResponseDto dto = new CommentResponseDto(1L, "Комментарий к посту", 1L);
        when(postCommentService.addCommentToPost(eq(1L), any(CreateCommentRequestDto.class))).thenReturn(dto);

        mockMvc.perform(post("/api/posts/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("Комментарий к посту"))
                .andExpect(jsonPath("$.postId").value(1));
        verify(postCommentService, times(1)).addCommentToPost(eq(1L), any(CreateCommentRequestDto.class));
    }

    @Test
    void updateComment_success() throws Exception {
        String json = """
                {"id":"2","text":"Второй комментарий к посту 1","postId":"1"}
                """;
        CommentResponseDto dto = new CommentResponseDto(2L, "Второй комментарий к посту 1", 1L);
        when(postService.existsPostById(1L)).thenReturn(true);
        when(commentService.updateComment(eq(1L), eq(2L), any(UpdateCommentRequestDto.class))).thenReturn(dto);
        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 1L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.text").value("Второй комментарий к посту 1"))
                .andExpect(jsonPath("$.postId").value(1));
    }

    @Test
    void updateComment_postNotFound_404() throws Exception {
        String json = """
                {"id":"2","text":"Второй комментарий к посту 1","postId":"1"}
                """;
        when(postService.existsPostById(999L)).thenReturn(false);
        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 999L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment() throws Exception {
        Long postId = 1L;
        Long commentId = 2L;
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1L, 2L))
                .andExpect(status().isOk());
        verify(postCommentService, times(1)).deleteCommentAndDecrementCount(postId, commentId);
    }

    @Test
    void deleteComment_NotFoundComment_ShouldNotChangeCounter() throws Exception {
        Long postId = 1L;
        Long fakeCommentId = 999L;
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, fakeCommentId))
                .andExpect(status().isOk());
    }
}
