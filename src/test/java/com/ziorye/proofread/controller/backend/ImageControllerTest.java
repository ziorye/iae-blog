package com.ziorye.proofread.controller.backend;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ziorye.proofread.controller.WithMockUserForAdminBaseTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.io.File;
import java.nio.file.Files;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.is;

@WithUserDetails(userDetailsServiceBeanName = "jpaUserDetailsService", value = "admin@example.com")
class ImageControllerTest extends WithMockUserForAdminBaseTest {
    @Autowired
    ObjectMapper objectMapper;
    @Autowired
    Environment env;

    @Test
    void uploadFromVditor() throws Exception {
        String originalFilename = "cover.png";
        MockMultipartFile coverFile = new MockMultipartFile("image[]", originalFilename, MediaType.IMAGE_PNG_VALUE, new byte[] { 1, 2, 3 });
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                        .multipart("/backend/images/uploadFromVditor")
                        .file(coverFile)
                )
                .andExpect(MockMvcResultMatchers.jsonPath("code", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("msg", is("success")))
                .andExpect(MockMvcResultMatchers.jsonPath("data.errFiles", empty()))
                .andExpect(MockMvcResultMatchers.jsonPath("data.succMap['" + originalFilename + "']").exists())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        String file = jsonNode.get("data").get("succMap").get(originalFilename).textValue();
        File coverOnDisk = new File(env.getProperty("custom.upload.base-path") + file);
        Assertions.assertTrue(Files.exists(coverOnDisk.toPath()));
        Assertions.assertTrue(coverOnDisk.delete());
    }

    @Test
    void uploadLinkFromPasteToVditor() throws Exception {
        String link = "http://xpicx.oss-cn-shenzhen.aliyuncs.com/uPic/send-email-2.png";
        MvcResult mvcResult = mvc.perform(MockMvcRequestBuilders
                        .post("/backend/images/uploadLinkFromPasteToVditor")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"url\":\"" + link + "\"}")
                )
                .andExpect(MockMvcResultMatchers.jsonPath("code", is(0)))
                .andExpect(MockMvcResultMatchers.jsonPath("msg", is("外部图片链接已成功上传到服务器")))
                .andExpect(MockMvcResultMatchers.jsonPath("data.originalURL", is(link)))
                .andExpect(MockMvcResultMatchers.jsonPath("data.url").exists())
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(mvcResult.getResponse().getContentAsString());
        String file = jsonNode.get("data").get("url").textValue();
        File coverOnDisk = new File(env.getProperty("custom.upload.base-path") + file);
        Assertions.assertTrue(Files.exists(coverOnDisk.toPath()));
        Assertions.assertTrue(coverOnDisk.delete());
    }
}