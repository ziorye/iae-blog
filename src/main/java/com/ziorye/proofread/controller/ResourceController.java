package com.ziorye.proofread.controller;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.service.PostService;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Optional;

@Slf4j
@Controller
@RequestMapping("/resources")
public class ResourceController {
    @Autowired
    PostService postService;

    @GetMapping
    String index(Model model,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(10);
        Page<Post> pageContent = postService.findAllResources(currentPage, pageSize);
        model.addAttribute("page", pageContent);
        return "resource/index";
    }

    @GetMapping("{id}")
    String show(@PathVariable Long id, Model model) {
        Optional<Post> optionalPost = postService.findById(id);

        if (optionalPost.isEmpty() || !optionalPost.get().isStatus()
                || !"resource".equals(optionalPost.get().getType())
        ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        model.addAttribute("post", optionalPost.get());
        return "resource/show";
    }

    @GetMapping("/download/{id}")
    @PreAuthorize("isAuthenticated()")
    void download(@PathVariable Long id, HttpServletResponse response, @AuthenticationPrincipal UserDetails user) throws IOException {
        Post post = postService.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        InputStream fis = getInputStream(post);

        ServletOutputStream sos = response.getOutputStream();
        response.setHeader("Content-Disposition", "attachment;filename=resource-" + id + "." + FilenameUtils.getExtension(post.getAttachment()));
        IOUtils.copy(fis, sos);
        fis.close();

        log.info(user.getUsername() + " downloaded resource[{}]", id);
    }

    @Value("${custom.upload.base-path}")
    String uploadBasePath;
    @Value("${custom.upload.chunked-dir-under-base-path}")
    String chunkedDirUnderBasePath;
    private InputStream getInputStream(Post post) {
        String attachment = post.getAttachment();

        if (!post.isStatus()
                || !"resource".equals(post.getType())
                || "".equals(attachment)
                || attachment == null
        ) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        if (attachment.startsWith("/" + chunkedDirUnderBasePath)) {
            try {
                return FileUtils.openInputStream(ResourceUtils.getFile("file:" + uploadBasePath + attachment));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else if(attachment.startsWith("http")) {
            try {
                return new URL(attachment).openStream();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            return getClass().getResourceAsStream("/static" + attachment);
        }
    }
}
