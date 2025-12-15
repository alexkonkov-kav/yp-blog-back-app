package com.blog.repository;

import com.blog.configuration.DataSourceConfig;
import com.blog.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringJUnitConfig(classes = {DataSourceConfig.class, JdbcNativePostRepository.class})
@TestPropertySource(locations = "classpath:application.properties")
public class JdbcNativePostRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private PostRepository postRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.update("INSERT INTO post (id, title, text) VALUES (?,?,?)", 1L, "Test title 1", "Test text 1");
        jdbcTemplate.update("INSERT INTO post (id, title, text) VALUES (?,?,?)", 2L, "Test title 2", "Test text 2");
        jdbcTemplate.update("INSERT INTO post (id, title, text) VALUES (?,?,?)", 3L, "Test title 3", "Test text 3");
    }

    @Test
    void findById_shouldReturnOnePost() {
        Post post = postRepository.findById(1L).orElse(null);

        assertNotNull(post);
        assertEquals(1L, post.getId());
        assertEquals("Test title 1", post.getTitle());
        assertEquals("Test text 1", post.getText());
    }
}
