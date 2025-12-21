package com.blog.repository;

import com.blog.model.Post;

import java.util.List;
import java.util.Optional;

public interface PostRepository {

    Post save(Post post);

    Optional<Post> findById(Long id);

    void update(Post post);

    void deleteById(Long id);

    void incrementLikesCount(Long id);

    boolean existsById(Long id);

    boolean updateImage(Long id, byte[] image);

    byte[] findImageById(Long id);

    void incrementCommentsCount(Long id);

    List<Post> findAll(String search, List<String> searchTags, int limit, int offset);

    long count(String search, List<String> searchTags);
}
