package com.blog.repository;

import com.blog.model.Comment;

import java.util.List;
import java.util.Optional;

public interface CommentRepository {

    Comment save(Comment comment);

    List<Comment> findByPostId(Long postId);

    Optional<Comment> findByIdAndPostId(Long id, Long postId);
}
