package com.blog.repository;

import com.blog.model.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@Import(JdbcNativeTagRepository.class)
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
        jdbcTemplate.execute("ALTER TABLE post ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE tag ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 1", "Test text 1");
        jdbcTemplate.update("INSERT INTO post (title, text) VALUES (?,?)", "Test title 2", "Test text 2");

        jdbcTemplate.update("INSERT INTO tag (name) VALUES (?)", "Test tag name 1");
        jdbcTemplate.update("INSERT INTO tag (name) VALUES (?)", "Test tag name 2");
        jdbcTemplate.update("INSERT INTO tag (name) VALUES (?)", "Test tag name 3");

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
        assertNotNull(tag.getId(), "ID должен быть сгенерирован");
        assertEquals("Test tag name 1", tag.getName());
        assertTrue(tags.stream().noneMatch(t -> t.getId().equals(3L)));
    }

    @Test
    void save_shouldCreateNewTag() {
        Tag newTag = new Tag("Test tag name 4");
        Tag savedTag = tagRepository.save(newTag);
        assertNotNull(savedTag.getId(), "ID должен быть сгенерирован");
        assertEquals("Test tag name 4", savedTag.getName());
        Optional<Tag> findTag = tagRepository.findByName("Test tag name 4");
        assertTrue(findTag.isPresent());
        assertEquals(savedTag.getId(), findTag.get().getId());
    }
}
