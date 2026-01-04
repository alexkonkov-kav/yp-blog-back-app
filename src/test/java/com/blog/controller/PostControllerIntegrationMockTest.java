package com.blog.controller;

import com.blog.dto.post.CreatePostRequestDto;
import com.blog.dto.post.PostResponseDto;
import com.blog.dto.post.UpdatePostRequestDto;
import com.blog.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PostController.class)
public class PostControllerIntegrationMockTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PostService postService;

    @Test
    void getPost() throws Exception {
        PostResponseDto postDto = new PostResponseDto();
        postDto.setId(1L);
        postDto.setTitle("Test title 1");
        postDto.setText("Test text 1");
        postDto.setTags(List.of("Test name 1", "Test name 2"));
        postDto.setLikesCount(0);
        postDto.setCommentsCount(0);
        when(postService.findById(1L)).thenReturn(postDto);

        mockMvc.perform(get("/api/posts/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.title").value("Test title 1"))
                .andExpect(jsonPath("$.text").value("Test text 1"))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", contains("Test name 1", "Test name 2")))
                .andExpect(jsonPath("$.likesCount").value("0"))
                .andExpect(jsonPath("$.commentsCount").value("0"));
    }

    @Test
    void savePost() throws Exception {
        String json = """
                {"title":"Название поста 3","text":"Текст поста в формате Markdown...","tags":["tag_1", "tag_2"]}
                """;
        PostResponseDto postDto = new PostResponseDto();
        postDto.setId(3L);
        postDto.setTitle("Название поста 3");
        postDto.setText("Текст поста в формате Markdown...");
        postDto.setTags(List.of("tag_1", "tag_2"));
        postDto.setLikesCount(0);
        postDto.setCommentsCount(0);

        when(postService.savePost(org.mockito.ArgumentMatchers.any(CreatePostRequestDto.class))).thenReturn(postDto);

        mockMvc.perform(post("/api/posts")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.title").value("Название поста 3"))
                .andExpect(jsonPath("$.text").value("Текст поста в формате Markdown..."))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag_1", "tag_2")));
    }

    @Test
    void updatePost() throws Exception {
        String json = """
                {"id":2,"title":"Название поста 3","text":"Текст поста в формате Markdown...","tags":["tag_1", "tag_2"]}
                """;
        PostResponseDto postDto = new PostResponseDto();
        postDto.setId(2L);
        postDto.setTitle("Название поста 3");
        postDto.setText("Текст поста в формате Markdown...");
        postDto.setTags(List.of("tag_1", "tag_2"));
        postDto.setLikesCount(0);
        postDto.setCommentsCount(0);
        when(postService.updatePost(org.mockito.ArgumentMatchers.any(UpdatePostRequestDto.class))).thenReturn(postDto);

        mockMvc.perform(put("/api/posts/{id}", 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.title").value("Название поста 3"))
                .andExpect(jsonPath("$.text").value("Текст поста в формате Markdown..."))
                .andExpect(jsonPath("$.tags", hasSize(2)))
                .andExpect(jsonPath("$.tags", containsInAnyOrder("tag_1", "tag_2")));

    }

    @Test
    void deletePost() throws Exception {
        mockMvc.perform(delete("/api/posts/1"))
                .andExpect(status().isOk());
        verify(postService, times(1)).deleteById(1L);
    }
}