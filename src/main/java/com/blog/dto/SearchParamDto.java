package com.blog.dto;

import java.util.List;

public class SearchParamDto {

    private String searchText;

    private List<String> tagNames;

    public SearchParamDto() {
    }

    public SearchParamDto(String searchText, List<String> tagNames) {
        this.searchText = searchText;
        this.tagNames = tagNames;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public List<String> getTagNames() {
        return tagNames;
    }

    public void setTagNames(List<String> tagNames) {
        this.tagNames = tagNames;
    }
}
