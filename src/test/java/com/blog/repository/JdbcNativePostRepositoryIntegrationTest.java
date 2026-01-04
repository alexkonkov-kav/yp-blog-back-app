package com.blog.repository;


import com.blog.model.Post;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJdbcTest
@Import(JdbcNativePostRepository.class)
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

    @Test
    void update_shouldModifyExistingPost() {
        Post existingPost = postRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Post with id: 1 not found"));
        existingPost.setTitle("Update Title");
        existingPost.setText("Update text");
        postRepository.update(existingPost);
        Post updatedPost = postRepository.findById(1L)
                .orElseThrow(() -> new AssertionError("Post with id: 1 not found after update"));
        assertEquals("Update Title", updatedPost.getTitle());
        assertEquals("Update text", updatedPost.getText());
        assertEquals(1L, updatedPost.getId());
        assertEquals(0, updatedPost.getLikesCount());
    }

    @Test
    void deleteById_shouldRemovePostAndPostTagFromDatabase() {
        postRepository.deleteById(1L);
        Optional<Post> deletedPost = postRepository.findById(1L);
        assertFalse(deletedPost.isPresent(), "Post with id: 1 not found");
    }

    @Test
    void updateImage() {
        byte[] image = new byte[]{1, 2, 3, 4};
        assertTrue(postRepository.updateImage(1L, image));
    }

    @Test
    void existsById_true_and_false() {
        assertTrue(postRepository.existsById(1L));
        assertFalse(postRepository.existsById(999L));
    }

    @Test
    void findImageById_returnsNull_whenNotSet() {
        assertNull(postRepository.findImageById(2L));
    }
}
