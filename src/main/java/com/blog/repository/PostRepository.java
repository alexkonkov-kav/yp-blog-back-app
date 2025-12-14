package com.blog.repository;

import com.blog.model.Post;

import java.util.Optional;

public interface PostRepository {

    Optional<Post> findById(Long id);
}
