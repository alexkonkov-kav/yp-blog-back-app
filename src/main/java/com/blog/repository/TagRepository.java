package com.blog.repository;

import com.blog.model.Tag;

import java.util.List;
import java.util.Optional;

public interface TagRepository {

    Tag save(Tag tag);

    Optional<Tag> findByName(String name);

    List<Tag> findByPostId(Long postId);
}
