package com.blog.repository;

import com.blog.model.Post;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativePostRepository implements PostRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Post save(Post post) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(e -> {
            PreparedStatement ps = e.prepareStatement(
                    "insert into post(title, text) values (?, ?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, post.getTitle());
            ps.setString(2, post.getText());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            post.setId(keyHolder.getKey().longValue());
        }
        return post;
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

    @Override
    public void update(Post post) {
        jdbcTemplate.update("update post set title = ?, text = ? where id = ?",
                post.getTitle(), post.getText(), post.getId());
    }

    @Override
    public void deleteById(Long id) {
        jdbcTemplate.update("delete from post where id = ?", id);
    }

    @Override
    public void incrementLikesCount(Long id) {
        jdbcTemplate.update("update post set likes_count = likes_count + 1 where id = ?", id);
    }

    @Override
    public boolean existsById(Long id) {
        Integer postCount = jdbcTemplate.queryForObject("select count(*) from post where id=?", Integer.class, id);
        return postCount != null && postCount > 0;
    }

    @Override
    public boolean updateImage(Long id, byte[] image) {
        int updated = jdbcTemplate.update("update post set image=? where id=?", image, id);
        return updated > 0;
    }
}
