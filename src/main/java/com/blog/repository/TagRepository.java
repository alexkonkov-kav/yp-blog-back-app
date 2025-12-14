package com.blog.repository;

import com.blog.model.Tag;

import java.util.List;

public interface TagRepository {

    List<Tag> findByPostId(Long postId);
}
