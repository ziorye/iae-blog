package com.ziorye.proofread.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

@Data
public class PostDto {
    private Long id;
    private Long user_id;

    @NotEmpty
    private String title;

    @NotEmpty
    private String content;

    private String description;

    private boolean status;

    private String cover;
}
