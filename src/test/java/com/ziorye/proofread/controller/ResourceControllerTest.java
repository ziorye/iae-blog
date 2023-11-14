package com.ziorye.proofread.controller;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.entity.User;
import com.ziorye.proofread.repository.PostRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class ResourceControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired
    PostRepository postRepository;

    @Test
    void showWithIncorrectType() throws Exception {
        String incorrectType = "post";
        Post post = new Post();
        post.setCreated_at(LocalDateTime.now());
        String title = UUID.randomUUID().toString();
        post.setTitle(title);
        post.setContent(UUID.randomUUID().toString());
        post.setUser(new User(1L));
        post.setType(incorrectType);
        postRepository.save(post);

        mvc.perform(MockMvcRequestBuilders
                        .get("/resources/" + post.getId()))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        ;

        mvc.perform(MockMvcRequestBuilders
                        .get("/posts/" + post.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
        ;

        postRepository.deleteById(post.getId());
    }
}