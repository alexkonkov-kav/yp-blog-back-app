package com.blog.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;

import java.util.List;

public class CreatePostRequestDto {

    @NotBlank(message = "Название поста обязательно")
    private String title;

    @NotBlank(message = "Текст поста обязателен")
    private String text;

    @NotEmpty(message = "Теги  обязательны")
    private List<String> tags;

    public CreatePostRequestDto(String title, String text, List<String> tags) {
        this.title = title;
        this.text = text;
        this.tags = tags;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }
}
