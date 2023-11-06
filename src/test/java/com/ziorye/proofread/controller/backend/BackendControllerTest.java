package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.bean.backend.BackendMenus;
import com.ziorye.proofread.controller.WithMockUserForAdminBaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class BackendControllerTest extends WithMockUserForAdminBaseTest {
    @Autowired
    BackendMenus backendMenus;

    @Test
    void dashboard() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/dashboard"))
                .andExpect(MockMvcResultMatchers.view().name("backend/dashboard"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("后台首页")))
        ;
    }

    @Test
    void empty() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/empty"))
                .andExpect(MockMvcResultMatchers.view().name("backend/empty"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("空白页")))
        ;
    }

    @Test
    void testAtModelAttribute() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/dashboard"))
                .andExpect(MockMvcResultMatchers.model().attributeExists("menus"))
                .andExpect(MockMvcResultMatchers.model().attribute("menus", backendMenus.getMenus()))
        ;
    }

    @Test
    void testRequestURIUsingAtModelAttribute() throws Exception {
        String path = "/backend/empty";
        mvc.perform(MockMvcRequestBuilders.get(path))
                .andExpect(MockMvcResultMatchers.model().attributeExists("requestURI"))
                .andExpect(MockMvcResultMatchers.model().attribute("requestURI", path))
        ;
    }

    @Test
    void testLogout() throws Exception {
        mvc.perform(MockMvcRequestBuilders.post("/logout"))
                .andExpect(SecurityMockMvcResultMatchers.unauthenticated())
                .andExpect(MockMvcResultMatchers.redirectedUrl("/"))
        ;
    }
}