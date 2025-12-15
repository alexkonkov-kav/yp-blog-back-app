package com.blog.repository;

import com.blog.configuration.DataSourceConfig;
import com.blog.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringJUnitConfig(classes = {DataSourceConfig.class, JdbcNativeTagRepository.class})
@TestPropertySource(locations = "classpath:application.properties")
public class JdbcNativeTagRepositoryIntegrationTest {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private TagRepository tagRepository;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("DELETE FROM post");
        jdbcTemplate.execute("DELETE FROM tag");
        jdbcTemplate.execute("DELETE FROM post_tag");
        jdbcTemplate.update("INSERT INTO post (id, title, text) VALUES (?,?,?)", 1L, "Test title 1", "Test text 1");
        jdbcTemplate.update("INSERT INTO post (id, title, text) VALUES (?,?,?)", 2L, "Test title 2", "Test text 2");

        jdbcTemplate.update("INSERT INTO tag (id, name) VALUES (?,?)", 1L, "Test tag name 1");
        jdbcTemplate.update("INSERT INTO tag (id, name) VALUES (?,?)", 2L, "Test tag name 2");
        jdbcTemplate.update("INSERT INTO tag (id, name) VALUES (?,?)", 3L, "Test tag name 3");

        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id) VALUES (?,?)", 1L, 1L);
        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id) VALUES (?,?)", 1L, 2L);
        jdbcTemplate.update("INSERT INTO post_tag (post_id, tag_id) VALUES (?,?)", 2L, 3L);

    }

    @Test
    void findByPostId_shouldReturnTagsForPostId() {
        List<Tag> tags = tagRepository.findByPostId(1L);

        assertNotNull(tags);
        assertEquals(2, tags.size());
        Tag tag = tags.getFirst();
        assertEquals(1L, tag.getId());
        assertEquals("Test tag name 1", tag.getName());
        assertTrue(tags.stream().noneMatch(t -> t.getId().equals(3L)));
    }
}
