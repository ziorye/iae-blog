package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.controller.WithMockUserForAdminBaseTest;
import com.ziorye.proofread.dto.PostDto;
import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.repository.PostRepository;
import com.ziorye.proofread.service.PostService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.nio.file.Files;
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

    @Test
    void storeWithCoverImage(@Autowired PostRepository postRepository, @Autowired Environment env) throws Exception {
        String title = "title-" + UUID.randomUUID();
        MockMultipartFile coverFile = new MockMultipartFile("coverFile", "cover.png", MediaType.IMAGE_PNG_VALUE, new byte[] { 1, 2, 3 });
        mvc.perform(MockMvcRequestBuilders
                        .multipart("/backend/post/store")
                        //.contentType(MediaType.MULTIPART_FORM_DATA)
                        .file(coverFile)
                        .param("id", "")
                        .param("user_id", "1")
                        .param("title", title)
                        .param("content", "content-" + UUID.randomUUID())
                )
                .andExpect(MockMvcResultMatchers.redirectedUrl("/backend/posts"))
        ;

        Optional<Post> po = postRepository.findFirstByTitle(title);
        Assertions.assertTrue(po.isPresent());

        String cover = po.get().getCover();
        File coverOnDisk = new File(env.getProperty("custom.upload.base-path") + cover);
        Assertions.assertTrue(Files.exists(coverOnDisk.toPath()));
        Assertions.assertTrue(coverOnDisk.delete());

        postRepository.delete(po.get());
    }

    @Test
    void indexWithSortByIdDescending(@Autowired PostService postService, @Autowired PostRepository postRepository) throws Exception {
        PostDto postDto = new PostDto();
        postDto.setUser_id(1L);
        postDto.setTitle("title-" + UUID.randomUUID());
        postDto.setContent("content-" + UUID.randomUUID());
        Post post = postService.savePost(postDto);

        mvc.perform(MockMvcRequestBuilders.get("/backend/posts"))
                .andExpect(MockMvcResultMatchers.view().name("backend/post/index"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(postDto.getTitle())))
        ;

        postRepository.delete(post);
    }

    @Test
    void update(@Autowired PostRepository postRepository) throws Exception {
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
        Post post = po.get();

        String descriptionUpdated = "description--updated";
        String contendUpdated = post.getContent() + "--updated";
        mvc.perform(MockMvcRequestBuilders.put("/backend/post/update")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", post.getId().toString())
                        .param("title", post.getTitle())
                        .param("user_id", "1")
                        .param("description", descriptionUpdated)
                        .param("content", contendUpdated)
                )
                .andExpect(MockMvcResultMatchers.redirectedUrl("/backend/posts"))
        ;

        Post postUpdated = postRepository.findFirstByTitle(title).get();
        Assertions.assertEquals(descriptionUpdated, postUpdated.getDescription());
        Assertions.assertEquals(contendUpdated, postUpdated.getContent());

        postRepository.delete(po.get());
    }

    @Test
    void updatePostThatNotMyOwn(@Autowired PostRepository postRepository) throws Exception {
        String title = "title-" + UUID.randomUUID();
        String authorId = "2";
        mvc.perform(MockMvcRequestBuilders.post("/backend/post/store")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", "")
                        .param("user_id", authorId)
                        .param("title", title)
                        .param("content", "content-" + UUID.randomUUID())
                )
                .andExpect(MockMvcResultMatchers.redirectedUrl("/backend/posts"))
        ;
        Optional<Post> po = postRepository.findFirstByTitle(title);
        Assertions.assertTrue(po.isPresent());
        Post post = po.get();

        String descriptionUpdated = "description--updated";
        String contendUpdated = post.getContent() + "--updated";
        mvc.perform(MockMvcRequestBuilders.put("/backend/post/update")
                        .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                        .param("id", post.getId().toString())
                        .param("title", post.getTitle())
                        .param("user_id", authorId)
                        .param("description", descriptionUpdated)
                        .param("content", contendUpdated)
                )
                .andExpect(MockMvcResultMatchers.status().isForbidden())
        ;

        postRepository.delete(po.get());
    }
}