package com.ziorye.proofread.repository;

import com.ziorye.proofread.entity.Post;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long> {
    Optional<Post> findFirstByTitle(String title);
}
