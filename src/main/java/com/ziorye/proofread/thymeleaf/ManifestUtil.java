package com.ziorye.proofread.thymeleaf;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class ManifestUtil {

    private final Manifest manifest;
    private static final String PREFIX = "/build/";

    public ManifestUtil() {
        try {
            InputStream inputStream = getClass().getResourceAsStream("/static/build/manifest.json");
            assert inputStream != null;
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            String contents = reader.lines().collect(Collectors.joining(System.lineSeparator()));
            ObjectMapper objectMapper = new ObjectMapper();
            this.manifest = objectMapper.readValue(contents, Manifest.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getJs() {
        return PREFIX + this.manifest.getJs().getFile();
    }

    public String getCss() {
        return PREFIX + this.manifest.getCss().getFile();
    }

    public static void main(String[] args) {
        System.out.println(new ManifestUtil().getJs());
        System.out.println(new ManifestUtil().getCss());
    }
}