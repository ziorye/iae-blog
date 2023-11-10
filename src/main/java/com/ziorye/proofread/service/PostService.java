package com.ziorye.proofread.service;

import com.ziorye.proofread.dto.PostDto;
import com.ziorye.proofread.entity.Post;
import org.springframework.data.domain.Page;

import java.util.Optional;

public interface PostService {
    Page<Post> findAll(int pageNumber, int pageSize);

    Post savePost(PostDto postDto);

    Optional<Post> findById(Long id);
}
