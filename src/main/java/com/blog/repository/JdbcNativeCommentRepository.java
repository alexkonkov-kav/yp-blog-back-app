package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Comment> findByPostId(Long postId) {
        return jdbcTemplate
                .query("select id, text from comment where post_id = ?",
                        (rs, rowNum) -> {
                            Comment comment = new Comment();
                            comment.setId(rs.getLong("id"));
                            comment.setText(rs.getString("text"));
                            return comment;
                        }, postId);
    }
}
