package com.blog.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
public class JdbcNativePostTagRepository implements PostTagRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativePostTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void save(Long postId, Long tagId) {
        jdbcTemplate.update("insert into post_tag(post_id, tag_id) values(?, ?)",
                postId, tagId);
    }

    @Override
    public void delete(Long postId) {
        jdbcTemplate.update("delete from post_tag where post_id = ?", postId);
    }
}
