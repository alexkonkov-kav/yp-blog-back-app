package com.blog.repository;

import com.blog.model.Comment;

import java.util.List;

public interface CommentRepository {

    List<Comment> findByPostId(Long postId);
}
