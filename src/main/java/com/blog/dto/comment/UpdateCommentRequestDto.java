package com.blog.dto.comment;

import jakarta.validation.constraints.NotBlank;

public class UpdateCommentRequestDto {

    @NotBlank(message = "Идентификатор комментария обязателен")
    private Long id;

    @NotBlank(message = "Текст комментария обязателен")
    private String text;

    @NotBlank(message = "Идентификатор поста обязателен")
    private Long postId;

    public UpdateCommentRequestDto() {
    }

    public UpdateCommentRequestDto(Long id, String text, Long postId) {
        this.id = id;
        this.text = text;
        this.postId = postId;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
