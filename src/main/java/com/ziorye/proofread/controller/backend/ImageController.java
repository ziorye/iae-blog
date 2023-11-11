package com.ziorye.proofread.controller.backend;

import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.*;

@RestController
@RequestMapping("/backend/images/")
public class ImageController {
    @Value("${custom.upload.base-path}")
    String uploadBasePath;
    @Value("${custom.upload.from-vditor-dir-under-base-path}")
    String fromVditorDirUnderBasePath;

    @PostMapping("uploadFromVditor")
    public Map<String, Object> uploadFromVditor(@RequestParam("image[]") MultipartFile[] images) {
        List<String> errorFiles = new ArrayList<>();
        Map<String, String> imageMap = new HashMap<>();

        for (MultipartFile image : images) {
            if (!image.isEmpty()) {
                File dir = new File(uploadBasePath + File.separator + fromVditorDirUnderBasePath);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                String originalFilename = image.getOriginalFilename();
                assert originalFilename != null;
                String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
                String newFilename = UUID.randomUUID() + suffix;
                try {
                    image.transferTo(new File(dir.getAbsolutePath() + File.separator + newFilename));
                } catch (IOException e) {
                    errorFiles.add(originalFilename);
                }

                imageMap.put(originalFilename, "/" + fromVditorDirUnderBasePath + File.separator + newFilename);
            }
        }

        Data data = new Data();
        data.setErrFiles(errorFiles);
        data.setSuccMap(imageMap);

        Map<String, Object> result = new HashMap<>();
        if (errorFiles.isEmpty()) {
            result.put("msg", "success");
            result.put("code", 0);
        } else {
            result.put("msg", "error");
            result.put("code", -1);
        }
        result.put("data", data);

        return result;
    }

    @lombok.Data
    private static class Data {
        private List<String> errFiles;
        private Map<String, String> succMap;
    }

    @PostMapping("uploadLinkFromPasteToVditor")
    public Map<String, Object> uploadLinkFromPasteToVditor(@RequestBody() Map<String, String> body, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        if (!body.containsKey("url")) {
            result.put("msg", "无效的 url");
            result.put("code", 0);
            result.put("data", null);

            return result;
        }

        String url = body.get("url");

        Map<String, String> data = new HashMap<>();
        if (url.startsWith(request.getScheme() + "://" + request.getServerName() + ":" + request.getServerPort())) {
            result.put("msg", "无需额外处理，直接返回原图地址");
            result.put("code", 0);
            data.put("originalURL", url);
            data.put("url", url);
            result.put("data", data);

            return result;
        }

        File dir = new File(uploadBasePath + File.separator + fromVditorDirUnderBasePath);
        if (!dir.exists()) {
            dir.mkdirs();
        }

        String extension = FilenameUtils.getExtension(url);
        String newFilename = UUID.randomUUID() + "." + extension;
        try {
            FileUtils.copyURLToFile(new URL(url), new File(dir.getAbsolutePath() + File.separator + newFilename));
            result.put("msg", "外部图片链接已成功上传到服务器");
            result.put("code", 0);
            data.put("originalURL", url);
            data.put("url", "/" + fromVditorDirUnderBasePath + File.separator + newFilename);
            result.put("data", data);
        } catch (IOException e) {
            result.put("code", -1);
            result.put("msg", "图片处理异常：" + e.getMessage());
            data.put("originalURL", url);
            data.put("url", url); // 原图返回，避免前端显示异常。
            result.put("data", data);
            throw new RuntimeException(e);
        }

        return result;
    }
}
