package com.blog.repository;

import com.blog.model.Post;

import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void update(Post post);

    void deleteById(Long id);
}
