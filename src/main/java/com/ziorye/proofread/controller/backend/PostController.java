package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Optional;

@Controller
@RequestMapping("/backend")
public class PostController {
    @Autowired
    PostService postService;

    @GetMapping("posts")
    String index(Model model,
                 @RequestParam("page") Optional<Integer> page,
                 @RequestParam("size") Optional<Integer> size) {
        int currentPage = page.orElse(1);
        int pageSize = size.orElse(5);
        Page<Post> pageContent = postService.findAll(currentPage, pageSize);
        model.addAttribute("page", pageContent);
        return "backend/post/index";
    }
}