package com.blog.repository;

public interface PostTagRepository {

    void save(Long postId, Long tagId);

    void delete(Long postId);
}
