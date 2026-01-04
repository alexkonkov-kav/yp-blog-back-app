package com.blog.repository;

import com.blog.model.Tag;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.List;
import java.util.Optional;

@Repository
public class JdbcNativeTagRepository implements TagRepository {

    private final JdbcTemplate jdbcTemplate;

    public JdbcNativeTagRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Tag save(Tag tag) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.update(e -> {
            PreparedStatement ps = e.prepareStatement(
                    "insert into tag(name) values (?)",
                    Statement.RETURN_GENERATED_KEYS);
            ps.setString(1, tag.getName());
            return ps;
        }, keyHolder);
        if (keyHolder.getKey() != null) {
            tag.setId(keyHolder.getKey().longValue());
        }
        return tag;
    }

    @Override
    public Optional<Tag> findByName(String name) {
        List<Tag> tags = jdbcTemplate
                .query("select id, name from tag where name = ?",
                        (rs, rowNum) -> {
                            Tag tage = new Tag();
                            tage.setId(rs.getLong("id"));
                            tage.setName(rs.getString("name"));
                            return tage;
                        }, name);
        return tags.isEmpty() ? Optional.empty() : Optional.of(tags.getFirst());
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
