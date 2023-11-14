package com.ziorye.proofread.controller;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.entity.User;
import com.ziorye.proofread.repository.PostRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {
    @Autowired
    MockMvc mvc;

    @Autowired PostRepository postRepository;

    @Test
    void index() throws Exception {
        List<Long> ids = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Post post = new Post();
            post.setCreated_at(LocalDateTime.now());
            post.setTitle(UUID.randomUUID().toString());
            post.setContent(UUID.randomUUID().toString());
            post.setUser(new User(1L));
            postRepository.save(post);
            ids.add(post.getId());
        }

        String currentPageNumber = "2";
        mvc.perform(MockMvcRequestBuilders
                        .get("/posts")
                        .param("page", currentPageNumber)
                        .param("size", "1")
                )
                .andExpect(MockMvcResultMatchers.view().name("post/index"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("page"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("当前第 " + currentPageNumber + " 页")))
        ;

        postRepository.deleteAllById(ids);
    }

    @Test
    void show() throws Exception {
        Post post = new Post();
        post.setCreated_at(LocalDateTime.now());
        String title = UUID.randomUUID().toString();
        post.setTitle(title);
        post.setContent(UUID.randomUUID().toString());
        post.setUser(new User(1L));
        postRepository.save(post);

        mvc.perform(MockMvcRequestBuilders
                        .get("/posts/" + post.getId()))
                .andExpect(MockMvcResultMatchers.view().name("post/show"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("post"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(title)))
        ;

        postRepository.deleteById(post.getId());
    }

    @Test
    void showWithIncorrectType() throws Exception {
        String incorrectType = "resource";
        Post post = new Post();
        post.setCreated_at(LocalDateTime.now());
        String title = UUID.randomUUID().toString();
        post.setTitle(title);
        post.setContent(UUID.randomUUID().toString());
        post.setUser(new User(1L));
        post.setType(incorrectType);
        postRepository.save(post);

        mvc.perform(MockMvcRequestBuilders
                        .get("/posts/" + post.getId()))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        ;

        mvc.perform(MockMvcRequestBuilders
                        .get("/resources/" + post.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
        ;

        postRepository.deleteById(post.getId());
    }
}