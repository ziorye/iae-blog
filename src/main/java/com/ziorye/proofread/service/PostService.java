package com.ziorye.proofread.service;

import com.ziorye.proofread.entity.Post;
import org.springframework.data.domain.Page;

public interface PostService {
    Page<Post> findAll(int pageNumber, int pageSize);
}
