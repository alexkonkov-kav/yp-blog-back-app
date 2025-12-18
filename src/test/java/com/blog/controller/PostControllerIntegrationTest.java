package com.blog.controller;

import com.blog.WebConfiguration;
import com.blog.configuration.DataSourceConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
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
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM post_tag");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
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
}
