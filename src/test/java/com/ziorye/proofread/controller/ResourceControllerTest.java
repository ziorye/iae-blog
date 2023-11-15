package com.ziorye.proofread.controller;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.entity.User;
import com.ziorye.proofread.repository.PostRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
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

    @Test
    void downloadLoginRequired() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/resources/download/" + Long.MAX_VALUE))
                .andExpect(MockMvcResultMatchers.status().is3xxRedirection())
                .andExpect(MockMvcResultMatchers.redirectedUrlPattern("**/login"))
        ;
    }

    @Test
    @WithMockUser
    void downloadNonExistentResource() throws Exception {
        mvc.perform(MockMvcRequestBuilders
                        .get("/resources/download/" + Long.MAX_VALUE))
                .andExpect(MockMvcResultMatchers.status().isNotFound())
        ;
    }

    @Test
    @WithMockUser
    void download() throws Exception {
        String resourceType = "resource";
        Post post = new Post();
        post.setCreated_at(LocalDateTime.now());
        String title = UUID.randomUUID().toString();
        post.setTitle(title);
        post.setContent(UUID.randomUUID().toString());
        post.setUser(new User(1L));
        post.setType(resourceType);
        post.setAttachment("/attachment/stock-data-visualization-template.zip");
        postRepository.save(post);

        mvc.perform(MockMvcRequestBuilders
                        .get("/resources/download/" + post.getId()))
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.header().stringValues("Content-Disposition", Matchers.contains("attachment;filename=resource-" + post.getId() + ".zip")))
        ;

        postRepository.deleteById(post.getId());
    }
}