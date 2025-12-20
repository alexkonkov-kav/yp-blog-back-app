package com.blog.repository;

import com.blog.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);
}
