package com.blog.repository;

import com.blog.configuration.DataSourceConfig;
import com.blog.model.Comment;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfig.class, JdbcNativeCommentRepository.class})
@TestPropertySource(locations = "classpath:application.properties")
public class JdbcNativeCommentRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("DELETE FROM comment");
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE comment ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 1", "Test text 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 2", "Test text 2");

        jdbcTemplate.update("INSERT INTO comment (text, post_id) VALUES (?,?)", "Test text comment 1", 1L);
        jdbcTemplate.update("INSERT INTO comment (text, post_id) VALUES (?,?)", "Test text comment 2", 1L);
        jdbcTemplate.update("INSERT INTO comment (text, post_id) VALUES (?,?)", "Test text comment 3", 2L);
    }

    @Test
    void findByPostId_shouldReturnCommentsForSpecificPost() {
        List<Comment> commentsForPost1 = commentRepository.findByPostId(1L);
        assertNotNull(commentsForPost1);
        assertEquals(2, commentsForPost1.size(), "У первого поста должно быть 2 комментария");
        assertTrue(commentsForPost1.stream().anyMatch(c -> c.getText().equals("Test text comment 1")));
        assertTrue(commentsForPost1.stream().anyMatch(c -> c.getText().equals("Test text comment 2")));
    }

    @Test
    void findByPostId_shouldReturnEmptyListIfNoCommentsExist() {
        List<Comment> comments = commentRepository.findByPostId(999L);
        assertNotNull(comments);
        assertTrue(comments.isEmpty(), "Для несуществующего поста список должен быть пустым");
    }
}
