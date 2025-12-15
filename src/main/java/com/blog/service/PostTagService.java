package com.blog.service;

import com.blog.model.Post;
import com.blog.model.Tag;
import com.blog.repository.PostTagRepository;
import org.springframework.stereotype.Service;

@Service
public class PostTagService {

    private final PostTagRepository postTagRepository;

    public PostTagService(PostTagRepository postTagRepository) {
        this.postTagRepository = postTagRepository;
    }

    public void saveLinkPostTag(Post post) {
        postTagRepository.delete(post.getId());
        for (Tag tag : post.getTags()) {
            postTagRepository.save(post.getId(), tag.getId());
        }
    }
}
