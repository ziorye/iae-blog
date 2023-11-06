package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.bean.backend.BackendMenus;
import com.ziorye.proofread.controller.WithMockUserForAdminBaseTest;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

class UserControllerTest extends WithMockUserForAdminBaseTest {
    @Autowired
    BackendMenus backendMenus;

    @Test
    void users() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/backend/users"))
                .andExpect(MockMvcResultMatchers.view().name("backend/user/index"))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.containsString("用户管理")))
        ;
    }
}