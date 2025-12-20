package com.blog.repository;

import com.blog.model.Comment;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativeCommentRepository implements CommentRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeCommentRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Comment save(Comment comment) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(e -> {
            PreparedStatement ps = e.prepareStatement(
                    "insert into comment(text, post_id) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, comment.getText());
            ps.setLong(2, comment.getPost().getId());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            comment.setId(keyHolder.getKey().longValue());
        }
        return comment;
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
