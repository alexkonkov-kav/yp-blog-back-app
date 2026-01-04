package com.blog.mapper;

import com.blog.dto.comment.CommentResponseDto;
import com.blog.model.Comment;
import org.springframework.stereotype.Component;

@Component
public class CommentMapper {

    public CommentResponseDto mapToResponse(Comment comment, Long postId) {
        CommentResponseDto commentResponseDto = new CommentResponseDto();
        commentResponseDto.setId(comment.getId());
        commentResponseDto.setText(comment.getText());
        commentResponseDto.setPostId(postId);
        return commentResponseDto;
    }
}
