package com.ziorye.proofread.service.impl;

import com.ziorye.proofread.dto.PostDto;
import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.entity.User;
import com.ziorye.proofread.repository.PostRepository;
import com.ziorye.proofread.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    PostRepository postRepository;

    @Override
    public Page<Post> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize, Sort.by("id").descending());
        return this.postRepository.findAll(pageable);
    }

    @Override
    public Post savePost(PostDto postDto) {
        Post post = new Post();

        if (postDto.getId() != null) {
            post = postRepository.findById(postDto.getId()).get();
            post.setUpdated_at(LocalDateTime.now());
        } else {
            post.setCreated_at(LocalDateTime.now());
        }

        post.setTitle(postDto.getTitle());
        post.setContent(postDto.getContent());
        post.setDescription(postDto.getDescription());
        post.setCover(postDto.getCover());
        post.setUser(new User(postDto.getUser_id()));
        return postRepository.save(post);
    }

    @Override
    public Optional<Post> findById(Long id) {
        return this.postRepository.findById(id);
    }
}
