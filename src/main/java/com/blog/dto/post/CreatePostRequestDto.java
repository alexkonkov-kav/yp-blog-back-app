package com.blog.dto.post;

import java.util.List;

public record CreatePostRequestDto(String title, String text, List<String> tags) {
}
