package com.blog.repository;

import com.blog.model.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class JdbcNativeTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<Tag> findByPostId(Long postId) {
        return jdbcTemplate.query("select t.id, t.name from tag t join post_tag pt on t.id = pt.tag_id where pt.post_id = ?",
                (rs, rowNum) -> {
                    Tag tag = new Tag();
                    tag.setId(rs.getLong("id"));
                    tag.setName(rs.getString("name"));
                    return tag;
                }, postId);
    }
}
