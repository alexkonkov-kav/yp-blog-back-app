package com.blog.service;

import com.blog.dto.SearchParamDto;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class SearchParamService {

    public SearchParamService() {
    }

    public SearchParamDto parseSearchParam(String search) {
        if (search == null || search.trim().isEmpty()) {
            return new SearchParamDto("", List.of());
        }

        List<String> texts = new ArrayList<>();
        List<String> tags = new ArrayList<>();

        for (String string : search.trim().split("\\s+")) {
            if (string.startsWith("#")) {
                if (string.length() > 1) {
                    tags.add(string.substring(1).toLowerCase());
                }
            } else texts.add(string);
        }
        String resultText = String.join(" ", texts);
        return new SearchParamDto(resultText, tags);
    }
}
