package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Comment> findByIdAndPostId(Long id, Long postId) {
        List<Comment> comments = jdbcTemplate
                .query("select id, text from comment where id = ? and post_id = ?",
                        (rs, rowNum) -> {
                            Comment comment = new Comment();
                            comment.setId(rs.getLong("id"));
                            comment.setText(rs.getString("text"));
                            return comment;
                        }, id, postId);
        return comments.isEmpty() ? Optional.empty() : Optional.of(comments.getFirst());
    }
}
