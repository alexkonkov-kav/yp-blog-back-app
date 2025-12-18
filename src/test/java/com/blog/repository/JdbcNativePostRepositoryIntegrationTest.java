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
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 1", "Test text 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 2", "Test text 2");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 3", "Test text 3");
    }

    @Test
    void findById_shouldReturnOnePost() {
        Post post = postRepository.findById(1L).orElse(null);

        assertNotNull(post);
        assertNotNull(1L, "ID должен быть сгенерирован");
        assertEquals("Test title 1", post.getTitle());
        assertEquals("Test text 1", post.getText());
    }

    @Test
    void save_shouldCreateNewPost() {
        Post newPost = new Post("Test title 4", "Test text 4");
        Post savedPost = postRepository.save(newPost);
        assertNotNull(savedPost.getId(), "ID должен быть сгенерирован");
        assertEquals(savedPost.getTitle(), "Test title 4");
        assertEquals(savedPost.getText(), "Test text 4");
        assertEquals(savedPost.getLikesCount(), 0);
        assertEquals(savedPost.getCommentsCount(), 0);
    }
}
