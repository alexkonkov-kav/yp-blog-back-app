package com.blog.dto.post;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public class UpdatePostRequestDto {

    @NotNull(message = "Идентификатор поста обязателен")
    private Long id;

    @NotBlank(message = "Название поста обязательно")
    private String title;

    @NotBlank(message = "Текст поста обязателен")
    private String text;

    @NotEmpty(message = "Список тегов обязателен")
    private List<String> tags;

    public UpdatePostRequestDto() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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
