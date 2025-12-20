package com.blog.controller;

import com.blog.WebConfiguration;
import com.blog.configuration.DataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.web.SpringJUnitWebConfig;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringJUnitWebConfig({
        DataSourceConfig.class,
        WebConfiguration.class
})
@WebAppConfiguration
@TestPropertySource(locations = "classpath:application.properties")
public class PostControllerIntegrationTest {

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
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM post_tag");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comment ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE tag ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 1','Test text 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post (title, text)
                    VALUES ('Test title 2','Test text 2')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO tag (name)
                    VALUES ('Test name 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO tag (name)
                    VALUES ('Test name 2')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post_tag (post_id, tag_id)
                    VALUES (1,1)
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post_tag (post_id, tag_id)
                    VALUES (1,2)
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
    void getPost() throws Exception {
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
        Integer tagsLinkCount = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM post_tag WHERE post_id = 1", Integer.class);
        assertEquals(0, tagsLinkCount, "Связи Post_Tag должны быть удалены каскадно");
    }

    @Test
    void uploadAndDownloadImage_success() throws Exception {
        byte[] pngStub = new byte[]{(byte) 137, 80, 78, 71};
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", pngStub);

        mockMvc.perform(multipart("/api/posts/{id}/image", 1L).file(file).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isCreated())
                .andExpect(content().string("ok"));

        mockMvc.perform(get("/api/posts/{id}/image", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG))
                .andExpect(header().string("Cache-Control", "no-store"))
                .andExpect(content().bytes(pngStub));
    }

    @Test
    void uploadImage_emptyFile_badRequest() throws Exception {
        MockMultipartFile empty = new MockMultipartFile("image", "image.png", "image/png", new byte[0]);

        mockMvc.perform(multipart("/api/posts/{id}/image", 1L).file(empty).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("empty image"));
    }

    @Test
    void uploadImage_postNotFound_404() throws Exception {
        MockMultipartFile file = new MockMultipartFile("image", "image.png", "image/png", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/posts/{id}/image", 999L).file(file).with(request -> {
                    request.setMethod("PUT");
                    return request;
                }))
                .andExpect(status().isNotFound())
                .andExpect(content().string("post not found"));
    }

    @Test
    void getImage_postHasNoImage_404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 2L))
                .andExpect(status().isNotFound());
    }

    @Test
    void getImage_postNotFound_404() throws Exception {
        mockMvc.perform(get("/api/posts/{id}/image", 777L))
                .andExpect(status().isNotFound());
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
}
