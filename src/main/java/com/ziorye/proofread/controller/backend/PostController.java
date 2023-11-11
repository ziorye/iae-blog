package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.dto.PostDto;
import com.ziorye.proofread.entity.Post;
import com.ziorye.proofread.exception.PostNotFoundException;
import com.ziorye.proofread.service.PostService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller("backendPostController")
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

    @GetMapping("post/create")
    String create(Model model) {
        model.addAttribute("post", new Post());
        return "backend/post/create";
    }

    @Value("${custom.upload.base-path}")
    String uploadBasePath;
    @Value("${custom.upload.post-cover-dir-under-base-path}")
    String postCoverDirUnderBasePath;
    private void doPostCover(MultipartFile file, PostDto postDto) throws IOException {
        if (file != null && !file.isEmpty()) {
            File dir = new File(uploadBasePath + File.separator + postCoverDirUnderBasePath);
            if (!dir.exists()) {
                dir.mkdirs();
            }
            String originalFilename = file.getOriginalFilename();
            assert originalFilename != null;
            String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
            String newFilename = UUID.randomUUID() + suffix;
            file.transferTo(new File(dir.getAbsolutePath() + File.separator + newFilename));
            postDto.setCover("/" + postCoverDirUnderBasePath + File.separator + newFilename);
        }
    }

    @PostMapping("post/store")
    String store(@RequestParam(value = "coverFile", required = false) MultipartFile file, @Valid @ModelAttribute("post") PostDto postDto,
                 BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            return "backend/post/create";
        }

        doPostCover(file, postDto);

        postService.savePost(postDto);
        return "redirect:/backend/posts";
    }

    @GetMapping("post/edit/{id}")
    String edit(@PathVariable Long id, Model model) {
        Optional<Post> optionalPost = postService.findById(id);
        if (optionalPost.isEmpty()) {
            throw new PostNotFoundException();
        } else {
            Post post = optionalPost.get();
            model.addAttribute("post", post);
            return "backend/post/edit";
        }
    }

    @PutMapping("post/update")
    @PreAuthorize("#postDto.user_id == authentication.principal.user.id")
    String update(@RequestParam(value = "coverFile", required = false) MultipartFile file, @Valid @ModelAttribute("post") PostDto postDto, BindingResult result, Model model) throws IOException {
        if (result.hasErrors()) {
            model.addAttribute("post", postDto);
            return "backend/post/edit";
        }

        doPostCover(file, postDto);

        postService.savePost(postDto);

        return "redirect:/backend/posts";
    }

    @DeleteMapping("post/destroy/{id}")
    String destroy(@PathVariable Long id) {
        postService.destroy(id);
        return "redirect:/backend/posts";
    }

    @DeleteMapping("post/destroy")
    @ResponseBody
    String destroyBatch(@RequestParam(value = "ids[]") List<Long> ids) {
        postService.destroyAllById(ids);
        return "DONE";
    }
}
