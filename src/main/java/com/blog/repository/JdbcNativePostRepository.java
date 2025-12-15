package com.blog.repository;

import com.blog.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<Post> findById(Long id) {
        List<Post> posts = jdbcTemplate
                .query("select id, title, text, likes_count, comments_count from post where id = ?",
                        (rs, rowNum) -> {
                            Post post = new Post();
                            post.setId(rs.getLong("id"));
                            post.setTitle(rs.getString("title"));
                            post.setText(rs.getString("text"));
                            post.setLikesCount(rs.getInt("likes_count"));
                            post.setCommentsCount(rs.getInt("comments_count"));
                            return post;
                        }, id);
        return posts.isEmpty() ? Optional.empty() : Optional.of(posts.getFirst());
    }
}
