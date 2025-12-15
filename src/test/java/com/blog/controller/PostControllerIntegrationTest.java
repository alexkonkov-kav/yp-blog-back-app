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

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
        jdbcTemplate.execute("""
                    INSERT INTO post (id, title, text)
                    VALUES (1,'Test title 1','Test text 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO post (id, title, text)
                    VALUES (2,'Test title 2','Test text 2')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO tag (id, name)
                    VALUES (1,'Test name 1')
                """);
        jdbcTemplate.execute("""
                    INSERT INTO tag (id, name)
                    VALUES (2,'Test name 2')
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
}
