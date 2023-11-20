package com.ziorye.proofread.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.Environment;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

@SpringBootTest
@AutoConfigureMockMvc
class IndexControllerTest extends WithMockUserBaseTest {
    @Autowired
    MockMvc mvc;

    @Value("${spring.application.name}")
    String applicationName;

    @Autowired
    Environment env;

    @Test
    void index() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/"))
                .andExpect(MockMvcResultMatchers.view().name("index"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(applicationName)))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString(env.getProperty("spring.application.name"))))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("博客。记录零散的所思所想。")))
        ;
    }
}