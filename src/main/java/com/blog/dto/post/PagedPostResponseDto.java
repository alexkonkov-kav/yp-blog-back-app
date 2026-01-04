package com.blog.dto.post;

import java.util.List;

public class PagedPostResponseDto {

    private List<PostResponseDto> posts;

    private boolean hasPrev;

    private boolean hasNext;

    private Long lastPage;

    public PagedPostResponseDto() {
    }

    public PagedPostResponseDto(List<PostResponseDto> posts, boolean hasPrev, boolean hasNext, Long lastPage) {
        this.posts = posts;
        this.hasPrev = hasPrev;
        this.hasNext = hasNext;
        this.lastPage = lastPage;
    }

    public List<PostResponseDto> getPosts() {
        return posts;
    }

    public void setPosts(List<PostResponseDto> posts) {
        this.posts = posts;
    }

    public boolean isHasPrev() {
        return hasPrev;
    }

    public void setHasPrev(boolean hasPrev) {
        this.hasPrev = hasPrev;
    }

    public boolean isHasNext() {
        return hasNext;
    }

    public void setHasNext(boolean hasNext) {
        this.hasNext = hasNext;
    }

    public Long getLastPage() {
        return lastPage;
    }

    public void setLastPage(Long lastPage) {
        this.lastPage = lastPage;
    }
}
