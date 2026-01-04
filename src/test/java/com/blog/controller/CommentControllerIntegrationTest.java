package com.blog.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.hasSize;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@AutoConfigureMockMvc
public class CommentControllerIntegrationTest {

    @Autowired
    private WebApplicationContext wac;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        mockMvc = MockMvcBuilders.webAppContextSetup(wac).build();

        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("DELETE FROM comment");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comment ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 1','Test text 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 2','Test text 2')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO comment (text, post_id)
                    VALUES ('Test comment name 1',1)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO comment (text, post_id)
                    VALUES ('Test comment name 2',1)
                """);
    }


    @Test
    void getCommentsByPostId_returnsJsonArray() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/comments", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].text").value("Test comment name 1"))
                .andExpect(jsonPath("$[1].text").value("Test comment name 2"));
    }

    @Test
    void getCommentByPost() throws Exception {
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
        mockMvc.perform(post("/api/posts/{postId}/comments", 1L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.text").value("Комментарий к посту"))
                .andExpect(jsonPath("$.postId").value(1));
        Integer commentsInDb = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM comment WHERE post_id = ? AND text = ?",
                Integer.class, 1, "Комментарий к посту");
        assertEquals(1, commentsInDb, "Комментарий должен быть сохранен в таблице comment");
    }

    @Test
    void updateComment_success() throws Exception {
        String json = """
                {"id":"2","text":"Второй комментарий к посту 1","postId":"1"}
                """;
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
        mockMvc.perform(put("/api/posts/{postId}/comments/{commentId}", 999L, 2L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteComment() throws Exception {
        Long postId = 1L;
        Long commentId = 2L;
        jdbcTemplate.update("UPDATE post SET comments_count = 2 WHERE id = ?", postId);
        Integer beforeDeleteCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM comment WHERE id = ? AND post_id = ?", Integer.class, commentId, postId);
        assertEquals(1, beforeDeleteCount);
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", 1L, 2L))
                .andExpect(status().isOk());
        Integer afterDeleteCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM comment WHERE id = ? AND post_id = ?", Integer.class, commentId, postId);
        assertEquals(0, afterDeleteCount, "Комментарий должен быть удален");
        Integer postCommentsCount = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, postId);
        assertEquals(1, postCommentsCount, "Счетчик comments_count должен уменьшиться на 1");
    }

    @Test
    void deleteComment_NotFoundComment_ShouldNotChangeCounter() throws Exception {
        Long postId = 1L;
        Long fakeCommentId = 999L;
        jdbcTemplate.update("UPDATE post SET comments_count = 5 WHERE id = ?", postId);
        mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", postId, fakeCommentId))
                .andExpect(status().isOk());
        Integer postCommentsCount = jdbcTemplate.queryForObject(
                "SELECT comments_count FROM post WHERE id = ?", Integer.class, postId);
        assertEquals(5, postCommentsCount);
    }
}
