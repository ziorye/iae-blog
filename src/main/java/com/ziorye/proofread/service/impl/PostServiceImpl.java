package com.ziorye.proofread.service.impl;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.repository.PostRepository;
import com.ziorye.proofread.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class PostServiceImpl implements PostService {
    @Autowired
    PostRepository postRepository;

    @Override
    public Page<Post> findAll(int pageNumber, int pageSize) {
        Pageable pageable = PageRequest.of(pageNumber - 1, pageSize);
        return this.postRepository.findAll(pageable);
    }
}
