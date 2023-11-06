package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.controller.WithMockUserForAdminBaseTest;
import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.repository.PostRepository;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.Optional;
import java.util.UUID;

@WithUserDetails(userDetailsServiceBeanName = "jpaUserDetailsService", value = "admin@example.com")
class PostControllerTest extends WithMockUserForAdminBaseTest {

    @Test
    void index() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/posts"))
                .andExpect(MockMvcResultMatchers.view().name("backend/post/index"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("Post 管理")))
        ;
    }

    @Test
    void create() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/post/create"))
                .andExpect(MockMvcResultMatchers.view().name("backend/post/create"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("post"))
        ;
    }

    @Test
    void storeWithoutTitleAndContent() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/backend/post/store")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "")
                        .param("user_id", "1")
                        .param("title", "")
                        .param("content", "")
                )
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrorCode("post", "title", "NotEmpty"))
                .andExpect(MockMvcResultMatchers.model().attributeHasFieldErrorCode("post", "content", "NotEmpty"))
        ;
    }

    @Test
    void store(@Autowired PostRepository postRepository) throws Exception {
        String title = "title-" + UUID.randomUUID();
        mvc.perform(MockMvcRequestBuilders.post("/backend/post/store")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "")
                        .param("user_id", "1")
                        .param("title", title)
                        .param("content", "content-" + UUID.randomUUID())
                )
                .andExpect(MockMvcResultMatchers.redirectedUrl("/backend/posts"))
        ;

        Optional<Post> po = postRepository.findFirstByTitle(title);
        Assertions.assertTrue(po.isPresent());

        postRepository.delete(po.get());
    }
}