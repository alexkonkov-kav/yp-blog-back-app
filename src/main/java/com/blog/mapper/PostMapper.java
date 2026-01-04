package com.blog.mapper;

import com.blog.dto.post.PostResponseDto;
import com.blog.model.Post;
import com.blog.model.Tag;
import org.springframework.stereotype.Component;

@Component
public class PostMapper {

    public PostResponseDto mapToResponseDto(Post post) {
        PostResponseDto postResponseDto = new PostResponseDto();
        postResponseDto.setId(post.getId());
        postResponseDto.setTitle(post.getTitle());
        postResponseDto.setText(post.getText());
        postResponseDto.setTags(post.getTags().stream().map(Tag::getName).toList());
        postResponseDto.setLikesCount(post.getLikesCount());
        postResponseDto.setCommentsCount(post.getCommentsCount());
        return postResponseDto;
    }
}
