package com.ziorye.proofread.controller.backend;

import com.ziorye.proofread.util.SystemVarKit;
import jakarta.annotation.PostConstruct;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Slf4j
@Controller
@RequestMapping("/file")
public class ChunkedTransferController {
    @Value("${server.port}")
    String serverPort;
    @Value("${custom.upload.base-path}")
    String uploadBasePath;
    @Value("${custom.upload.chunked-dir-under-base-path}")
    String chunkedDirUnderBasePath;

    String chunkedDir;

    @PostConstruct
    public void init() {
        chunkedDir = uploadBasePath + File.separator + chunkedDirUnderBasePath;
        File file = new File(chunkedDir);
        if (!file.exists()) {
            boolean created = file.mkdirs();
            if (created) {
                log.info("Created dir:{}", chunkedDir);
            }
        }
    }

    @PostMapping("/process")
    @ResponseBody
    public String getUploadUniqueLocation(HttpServletRequest request) {
        String uploadLength = request.getHeader("Upload-Length");
        log.debug("uploadLength: {}", uploadLength);
        return UUID.randomUUID().toString().replaceAll("-", "");
    }

    @PatchMapping("/patch/{unique}")
    @ResponseBody
    public void receiveChunkFile(@PathVariable("unique") String unique,
                                 HttpServletRequest request) throws IOException {
        log.debug("unique: {}", unique);
        String uploadLength = request.getHeader("Upload-Length");
        log.debug("uploadLength: {}", uploadLength);
        String uploadOffset = request.getHeader("Upload-Offset");
        log.debug("uploadOffset: {}", uploadOffset);
        String uploadName = request.getHeader("Upload-Name");
        log.debug("uploadName: {}", uploadName);

        File tempChunkFileCacheDir =
                new File(SystemVarKit.getOsCacheDirPath() + File.separator + unique);
        if (!tempChunkFileCacheDir.exists()) {
            tempChunkFileCacheDir.mkdirs();
            log.debug("create temp dir: {}", tempChunkFileCacheDir);
        }

        ServletInputStream inputStream = request.getInputStream();
        byte[] bytes = inputStream.readAllBytes();
        Assert.notNull(bytes, "file bytes must not be null");

        long offset = Long.parseLong(uploadOffset) + bytes.length;
        File uploadedChunkCacheFile = new File(tempChunkFileCacheDir + File.separator + offset);
        Files.write(Path.of(uploadedChunkCacheFile.toURI()), bytes);
        log.debug("upload chunk[{}] to path: {}", uploadOffset,
                uploadedChunkCacheFile.getAbsolutePath());

        if (offset == Long.parseLong(uploadLength)) {
            String postfix = uploadName.substring(uploadName.lastIndexOf(".") + 1);
            meringTempChunkFile(unique, postfix);
        }
    }

    private void meringTempChunkFile(String unique, String postfix) throws IOException {
        log.debug("All chunks upload has finish, will start merging files");
        String url = "";

        File targetFile =
                new File(chunkedDir + File.separator + unique + "." + postfix);
        log.debug("upload target file path: {}", targetFile.getAbsolutePath());

        String chunkFileDirPath = SystemVarKit.getOsCacheDirPath() + File.separator + unique;
        File chunkFileDir = new File(chunkFileDirPath);
        File[] files = chunkFileDir.listFiles();
        assert files != null;
        List<File> chunkFileList = Arrays.asList(files);
        // 根据文件名(偏移量)升序, 不然合并的文件分片内容的顺序不正常
        chunkFileList.sort(new Comparator<>() {
            @Override
            public int compare(File o1, File o2) {
                long o1Offset = Long.parseLong(o1.getName());
                long o2Offset = Long.parseLong(o2.getName());
                if (o1Offset < o2Offset) {
                    return -1;
                } else if (o1Offset > o2Offset) {
                    return 1;
                }
                return 0;
            }

            @Override
            public boolean equals(Object obj) {
                return false;
            }
        });
        int targetFileWriteOffset = 0;
        for (File chunkFile : chunkFileList) {
            try (RandomAccessFile randomAccessFile = new RandomAccessFile(targetFile, "rw");
                 FileInputStream fileInputStream = new FileInputStream(chunkFile);) {
                randomAccessFile.seek(targetFileWriteOffset);
                byte[] bytes = new byte[fileInputStream.available()];
                int read = fileInputStream.read(bytes);
                randomAccessFile.write(bytes);
                targetFileWriteOffset += read;
                log.debug("[{}] current merge targetFileWriteOffset: {}", chunkFile.getName(), targetFileWriteOffset);
            }
        }

        url = path2url(targetFile.getAbsolutePath());

        log.debug("Merging all chunk files success, url: {}", url);
    }

    @PutMapping("/data")
    @ResponseBody
    public String uploadFileData(@RequestParam("file") MultipartFile multipartFile)
            throws IOException {
        Assert.notNull(multipartFile, "'multipartFile' must not be null");

        String originalFilename = multipartFile.getOriginalFilename();
        byte[] bytes = multipartFile.getBytes();
        Assert.notNull(bytes, "file bytes must not be null");
        Assert.isTrue(bytes.length > 0, "file bytes must >0");

        File uploadedFile =
                new File(chunkedDir + File.separator + originalFilename);
        Files.write(Path.of(uploadedFile.toURI()), bytes);

        return path2url(uploadedFile.getAbsolutePath());
    }

    private String path2url(String path) {
        String url = "";
        String currentAppDirPath = SystemVarKit.getCurrentAppDirPath();
        String ipAddress = SystemVarKit.getIPAddress();
        String baseUrl = "http://" + ipAddress + ":" + serverPort;
        url = path.replace(currentAppDirPath, baseUrl);
        // 如果是 ntfs 目录 URL，则需要替换下 \ 为 /
        if (url.indexOf("\\") > 0) {
            url = url.replace("\\", "/");
        }
        return url;
    }
}