package com.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;

public class CreateCommentRequestDto {

    @NotBlank(message = "Текст комментария обязателен")
    private String text;

    @NotBlank(message = "Идентификатор поста обязателен")
    private Long postId;

    public CreateCommentRequestDto() {
    }

    public CreateCommentRequestDto(String text, Long postId) {
        this.text = text;
        this.postId = postId;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Long getPostId() {
        return postId;
    }

    public void setPostId(Long postId) {
        this.postId = postId;
    }
}
